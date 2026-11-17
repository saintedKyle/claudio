package com.claudio.app

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.claudio.app.data.ChatMessage
import com.claudio.app.data.HistoryStore
import com.claudio.app.data.Session
import com.claudio.app.data.Settings
import com.claudio.app.data.SettingsStore
import com.claudio.app.net.OpenRouterClient
import com.claudio.app.util.ImageUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClaudioViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = SettingsStore(app)
    private val historyStore = HistoryStore(app)

    val settings: StateFlow<Settings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _current = MutableStateFlow<Session?>(null)
    val current: StateFlow<Session?> = _current

    private val _streaming = MutableStateFlow(false)
    val streaming: StateFlow<Boolean> = _streaming

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _pendingImage = MutableStateFlow<ImageBitmap?>(null)
    val pendingImage: StateFlow<ImageBitmap?> = _pendingImage

    private val _loadingImage = MutableStateFlow(false)
    val loadingImage: StateFlow<Boolean> = _loadingImage

    private var pendingBase64: String? = null
    private var streamJob: Job? = null

    init {
        viewModelScope.launch { _sessions.value = historyStore.loadAll() }
    }

    fun saveSettings(apiKey: String, baseUrl: String, model: String) {
        viewModelScope.launch { settingsStore.save(apiKey, baseUrl, model) }
    }

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            _loadingImage.value = true
            _pendingImage.value = null
            pendingBase64 = null
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val bitmap = ImageUtils.loadScaled(getApplication(), uri)
                    bitmap?.let { it.asImageBitmap() to ImageUtils.toBase64(it) }
                }.getOrNull()
            }
            if (result != null) {
                _pendingImage.value = result.first
                pendingBase64 = result.second
            } else {
                _error.value = "Couldn't read that image. Try taking the photo again."
            }
            _loadingImage.value = false
        }
    }

    fun clearPending() {
        _pendingImage.value = null
        pendingBase64 = null
    }

    fun startSession(question: String) {
        val base64 = pendingBase64 ?: return
        val trimmed = question.trim()
        val session = Session(
            title = trimmed.ifBlank { "Photo question" }.take(60),
            model = settings.value.model,
            messages = listOf(ChatMessage(role = "user", text = trimmed, imageBase64 = base64))
        )
        _current.value = session
        clearPending()
        runCompletion()
    }

    fun followUp(text: String) {
        val session = _current.value ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _streaming.value) return
        _current.value = session.copy(
            messages = session.messages + ChatMessage(role = "user", text = trimmed),
            updatedAt = System.currentTimeMillis()
        )
        runCompletion()
    }

    fun retry() {
        val session = _current.value ?: return
        val messages = if (session.messages.lastOrNull()?.role == "assistant") session.messages.dropLast(1) else session.messages
        _current.value = session.copy(messages = messages)
        runCompletion()
    }

    fun stopStreaming() {
        streamJob?.cancel()
    }

    fun openSession(id: String) {
        _error.value = null
        _current.value = _sessions.value.find { it.id == id }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            historyStore.delete(id)
            if (_current.value?.id == id) _current.value = null
            _sessions.value = historyStore.loadAll()
        }
    }

    fun dismissError() {
        _error.value = null
    }

    private fun runCompletion() {
        streamJob?.cancel()
        val base = _current.value ?: return
        streamJob = viewModelScope.launch {
            _streaming.value = true
            _error.value = null
            var session = base.copy(
                messages = base.messages + ChatMessage(role = "assistant", text = ""),
                updatedAt = System.currentTimeMillis()
            )
            _current.value = session
            try {
                OpenRouterClient.streamChat(settings.value, base.messages).collect { delta ->
                    val messages = session.messages.toMutableList()
                    val last = messages.last()
                    messages[messages.lastIndex] = last.copy(text = last.text + delta)
                    session = session.copy(messages = messages)
                    _current.value = session
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Something went wrong. Please try again."
            } finally {
                _streaming.value = false
                val last = session.messages.lastOrNull()
                if (last?.role == "assistant" && last.text.isBlank()) {
                    session = session.copy(messages = session.messages.dropLast(1))
                    _current.value = session
                }
                withContext(NonCancellable) {
                    if (session.messages.isNotEmpty()) historyStore.save(session)
                    _sessions.value = historyStore.loadAll()
                }
            }
        }
    }
}