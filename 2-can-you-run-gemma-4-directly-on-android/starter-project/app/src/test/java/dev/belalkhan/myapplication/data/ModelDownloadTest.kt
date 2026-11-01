package dev.belalkhan.myapplication.data

import java.io.File
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ModelDownloadTest {
    @get:Rule val files = TemporaryFolder()
    private lateinit var server: MockWebServer
    private lateinit var downloader: ModelDownloader
    private val filename = "gemma-4-e2b-it.litertlm"
    private val part get() = File(files.root, "$filename.part")

    @Before fun setUp() {
        val certificate = HeldCertificate.Builder().commonName("localhost")
            .addSubjectAlternativeName("localhost").build()
        val serverCertificates = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        val clientCertificates = HandshakeCertificates.Builder()
            .addTrustedCertificate(certificate.certificate).build()
        server = MockWebServer()
        server.useHttps(serverCertificates.sslSocketFactory())
        server.start()
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .followRedirects(false).followSslRedirects(false).build()
        downloader = ModelDownloader(files.root, client, "test-token", server.url("/model").toString())
    }

    @After fun tearDown() { server.close() }

    @Test fun completeDownloadIsPublishedAndSurvivesRestart() = runBlocking {
        server.enqueue(MockResponse.Builder().body("abcdef").build())
        val progress = mutableListOf<Float>()
        downloader.download { progress += it }
        assertEquals("abcdef", downloader.modelFile.readText())
        assertTrue(downloader.isModelDownloaded())
        assertFalse(part.exists())
        assertEquals(1f, progress.last())
        downloader.download { fail("A complete model must not download again") }
        assertEquals(1, server.requestCount)
    }

    @Test fun resumesPartialDownloadAcrossRedirectWithoutSendingTokenToStorage() = runBlocking {
        part.writeText("abc")
        server.enqueue(MockResponse.Builder().code(302).addHeader("Location", "/storage").build())
        server.enqueue(MockResponse.Builder().code(206)
            .addHeader("Content-Range", "bytes 3-5/6").body("def").build())
        downloader.download {}
        repeat(2) {
            val request = server.takeRequest()
            assertEquals("bytes=3-", request.headers["Range"])
            assertNull(request.headers["Authorization"])
        }
        assertEquals("abcdef", downloader.modelFile.readText())
        assertTrue(downloader.isModelDownloaded())
    }

    @Test fun replacesPartialFileWhenServerIgnoresRange() = runBlocking {
        part.writeText("old")
        server.enqueue(MockResponse.Builder().body("abcdef").build())
        downloader.download {}
        assertEquals("abcdef", downloader.modelFile.readText())
    }

    @Test fun incompleteRangeNeverBecomesValidModel() = runBlocking {
        part.writeText("abc")
        server.enqueue(MockResponse.Builder().code(206)
            .addHeader("Content-Range", "bytes 3-4/6").body("de").build())
        assertTrue(runCatching { downloader.download {} }.isFailure)
        assertFalse(downloader.isModelDownloaded())
        assertFalse(downloader.modelFile.exists())
        assertEquals("abcde", part.readText())
    }

    @Test fun validatesAlreadyCompletePartialFileWithServerTotal() = runBlocking {
        part.writeText("abcdef")
        server.enqueue(MockResponse.Builder().code(416)
            .addHeader("Content-Range", "bytes */6").build())
        downloader.download {}
        assertTrue(downloader.isModelDownloaded())
        assertEquals("abcdef", downloader.modelFile.readText())
    }

    @Test fun rejectsUnverifiedFinalFileUntilServerConfirmsSize() {
        downloader.modelFile.writeText("incomplete")
        assertFalse(downloader.isModelDownloaded())
    }

    @Test fun refusesRedirectToUnencryptedConnection() = runBlocking {
        server.enqueue(MockResponse.Builder().code(302)
            .addHeader("Location", "http://localhost:${server.port}/storage").build())
        assertTrue(runCatching { downloader.download {} }.isFailure)
        assertFalse(downloader.isModelDownloaded())
        assertEquals(1, server.requestCount)
    }

    @Test fun bearerTokenIsOnlySentToHuggingFaceAndRangeSurvivesRedirect() = runBlocking {
        part.writeText("abc")
        val requests = mutableListOf<Request>()
        val client = OkHttpClient.Builder().followRedirects(false).addInterceptor { chain ->
            requests += chain.request()
            val response = Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
            if (requests.size == 1) {
                response.code(302).message("Found")
                    .header("Location", "https://storage.example/model")
                    .body("".toResponseBody()).build()
            } else {
                response.code(206).message("Partial Content")
                    .header("Content-Range", "bytes 3-5/6")
                    .body("def".toResponseBody()).build()
            }
        }.build()
        val model = ModelDownloader(files.root, client, "test-token")
        model.download {}
        assertEquals("Bearer test-token", requests[0].header("Authorization"))
        assertNull(requests[1].header("Authorization"))
        assertTrue(requests.all { it.header("Range") == "bytes=3-" })
        assertTrue(model.isModelDownloaded())
    }
}
