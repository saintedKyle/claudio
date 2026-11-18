package com.claudio.app

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.claudio.app.data.ChatMessage
import com.claudio.app.data.HistoryStore
import com.claudio.app.data.ModelInfo
import com.claudio.app.data.Session
import com.claudio.app.data.Settings
import com.claudio.app.data.SettingsStore
import com.claudio.app.net.OpenRouterClient
import com.claudio.app.net.StreamEvent
import com.claudio.app.util.ImageUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    private var modelsJob: Job? = null

    private val _freeModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val freeModels: StateFlow<List<ModelInfo>> = _freeModels

    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading

    private val _modelsError = MutableStateFlow<String?>(null)
    val modelsError: StateFlow<String?> = _modelsError

    init {
        viewModelScope.launch { _sessions.value = historyStore.loadAll() }
        viewModelScope.launch {
            val s = settings.first { it.loaded }
            if (s.apiKey.isNotBlank()) fetchModels(s.apiKey, s.baseUrl)
        }
    }

    fun saveSettings(apiKey: String, baseUrl: String, model: String) {
        viewModelScope.launch {
            settingsStore.save(apiKey, baseUrl, model)
            fetchModels(apiKey, baseUrl)
        }
    }

    fun fetchModels(apiKey: String, baseUrl: String) {
        if (apiKey.isBlank()) return
        modelsJob?.cancel()
        modelsJob = viewModelScope.launch {
            _modelsLoading.value = true
            _modelsError.value = null
            try {
                val list = OpenRouterClient.fetchFreeModels(apiKey, baseUrl)
                _freeModels.value = list
                if (list.isEmpty()) {
                    _modelsError.value = "This provider returned no free models. Curated picks are shown instead."
                } else if (settings.value.model == Settings.DEFAULT_MODEL) {
                    val pick = list.firstOrNull { it.vision } ?: list.first()
                    settingsStore.save(settings.value.apiKey, settings.value.baseUrl, pick.id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _modelsError.value = e.message ?: "Couldn't load the model list."
            } finally {
                _modelsLoading.value = false
            }
        }
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
            var reasoningStart = 0L
            try {
                OpenRouterClient.streamChat(settings.value, base.messages).collect { event ->
                    val messages = session.messages.toMutableList()
                    val last = messages.last()
                    val updated = when (event) {
                        is StreamEvent.Reasoning -> {
                            if (reasoningStart == 0L) reasoningStart = System.currentTimeMillis()
                            last.copy(reasoning = (last.reasoning ?: "") + event.text)
                        }
                        is StreamEvent.Content -> {
                            val elapsed = if (reasoningStart != 0L && last.reasoningMs == null) {
                                System.currentTimeMillis() - reasoningStart
                            } else last.reasoningMs
                            last.copy(text = last.text + event.text, reasoningMs = elapsed)
                        }
                    }
                    messages[messages.lastIndex] = updated
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
                if (last?.role == "assistant") {
                    if (last.text.isBlank()) {
                        session = session.copy(messages = session.messages.dropLast(1))
                        _current.value = session
                    } else if (reasoningStart != 0L && last.reasoningMs == null) {
                        val messages = session.messages.toMutableList()
                        messages[messages.lastIndex] = last.copy(reasoningMs = System.currentTimeMillis() - reasoningStart)
                        session = session.copy(messages = messages)
                        _current.value = session
                    }
                }
                withContext(NonCancellable) {
                    if (session.messages.isNotEmpty()) historyStore.save(session)
                    _sessions.value = historyStore.loadAll()
                }
            }
        }
    }
}