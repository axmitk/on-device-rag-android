package dev.belalkhan.myapplication.ai

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.Bitmap
import android.net.Uri
import com.google.ai.edge.litertlm.*
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dev.belalkhan.myapplication.data.ModelDownloader
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@ViewModelScoped
class GemmaChat @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloader: ModelDownloader,
) {
    private val mutex = Mutex()
    private var engine: Engine? = null
    private var conversation: Conversation? = null

    suspend fun sendMessage(prompt: String, imageUri: Uri?, onToken: (String) -> Unit) =
        withContext(Dispatchers.Default) {
            mutex.withLock {
                initialize()
                val current = checkNotNull(conversation)
                val content = buildList {
                    imageUri?.let { add(Content.ImageBytes(readImage(it))) }
                    add(Content.Text(prompt.ifBlank { "What do you see in this image?" }))
                }
                currentCoroutineContext().ensureActive()
                val finished = CompletableDeferred<Unit>()
                try {
                    current.sendMessageAsync(Contents.of(content), object : MessageCallback {
                        override fun onMessage(message: Message) {
                            if (!finished.isCompleted) {
                                message.contents.contents.filterIsInstance<Content.Text>()
                                    .forEach { onToken(it.text) }
                            }
                        }
                        override fun onDone() { finished.complete(Unit) }
                        override fun onError(throwable: Throwable) {
                            finished.completeExceptionally(throwable)
                        }
                    })
                    finished.await()
                } catch (cancelled: CancellationException) {
                    current.cancelProcess()
                    // Keep the lock until the native callback finishes before closing resources.
                    withContext(NonCancellable) { runCatching { finished.await() } }
                    throw cancelled
                } catch (failure: Exception) {
                    current.close()
                    conversation = null
                    throw failure
                }
            }
        }

    suspend fun clearConversation() = mutex.withLock {
        conversation?.close()
        conversation = null
    }

    fun release() {
        val cleanup = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        cleanup.launch {
            try {
                mutex.withLock {
                    try { conversation?.close() } finally {
                        conversation = null
                        engine?.close()
                        engine = null
                    }
                }
            } finally { cleanup.cancel() }
        }
    }

    private fun initialize() {
        if (conversation != null) return
        if (engine == null) {
            check(modelDownloader.isModelDownloaded()) { "Download the model before chatting." }
            val candidate = Engine(EngineConfig(
                modelPath = modelDownloader.modelFile.absolutePath,
                backend = Backend.CPU(),
                visionBackend = Backend.CPU(),
                maxNumImages = 1,
                cacheDir = context.cacheDir.absolutePath,
            ))
            try { candidate.initialize() } catch (failure: Exception) {
                runCatching { candidate.close() }
                throw failure
            }
            engine = candidate
        }
        conversation = checkNotNull(engine).createConversation(ConversationConfig(
            samplerConfig = SamplerConfig(topK = 20, topP = 0.9, temperature = 0.7),
        ))
    }

    private fun readImage(uri: Uri): ByteArray {
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val scale = (1536f / maxOf(info.size.width, info.size.height)).coerceAtMost(1f)
            decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        return try {
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) { "Could not prepare this image." }
                output.toByteArray()
            }
        } finally { bitmap.recycle() }
    }
}
