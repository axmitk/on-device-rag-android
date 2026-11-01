package dev.belalkhan.myapplication.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.myapplication.ai.GemmaChat
import dev.belalkhan.myapplication.data.ModelDownloader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val downloader: ModelDownloader,
    private val chat: GemmaChat,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatUiState(isModelDownloaded = downloader.isModelDownloaded()))
    val uiState = mutableState.asStateFlow()
    private val ids = AtomicLong()

    fun downloadModel() = work {
        downloader.download { progress -> mutableState.update { it.copy(downloadProgress = progress) } }
        mutableState.update { it.copy(isModelDownloaded = true, downloadProgress = 1f) }
    }

    fun sendMessage(prompt: String, imageUri: Uri?) {
        if (!uiState.value.isModelDownloaded || (prompt.isBlank() && imageUri == null)) return
        val userId = ids.incrementAndGet()
        val assistantId = ids.incrementAndGet()
        val text = prompt.trim().ifBlank { "What do you see in this image?" }
        work(prepare = {
            it.copy(messages = it.messages +
                ChatMessage(userId, text, isUser = true, imageUri = imageUri) +
                ChatMessage(assistantId, "", isUser = false, isStreaming = true))
        }) {
            try {
                chat.sendMessage(text, imageUri) { token ->
                    mutableState.update { state -> state.copy(messages = state.messages.map {
                        if (it.id == assistantId) it.copy(text = it.text + token) else it
                    }) }
                }
            } finally {
                mutableState.update { state -> state.copy(messages = state.messages.map {
                    if (it.id == assistantId) it.copy(isStreaming = false,
                        text = it.text.ifBlank { "No response was generated. Please try again." }) else it
                }) }
            }
        }
    }

    fun clearChat() = work {
        chat.clearConversation()
        mutableState.update { it.copy(messages = emptyList()) }
    }

    fun dismissError() { mutableState.update { it.copy(error = null) } }

    private fun work(
        prepare: (ChatUiState) -> ChatUiState = { it },
        action: suspend () -> Unit,
    ) {
        // Reserve the operation synchronously before launching background work.
        while (true) {
            val state = mutableState.value
            if (state.isBusy) return
            if (mutableState.compareAndSet(state, prepare(state).copy(isBusy = true, error = null))) break
        }
        viewModelScope.launch(Dispatchers.Default) {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                mutableState.update { it.copy(error = failure.message ?: "Something went wrong. Please try again.") }
            } finally { mutableState.update { it.copy(isBusy = false) } }
        }
    }

    override fun onCleared() { chat.release() }
}
