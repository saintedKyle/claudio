package com.claudio.app

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.claudio.app.data.AppConfig
import com.claudio.app.data.ChatMessage
import com.claudio.app.data.HistoryStore
import com.claudio.app.data.ModelInfo
import com.claudio.app.data.Session
import com.claudio.app.data.Settings
import com.claudio.app.data.SettingsStore
import com.claudio.app.data.ThemePrefs
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
    private var imageJob: Job? = null
    private var imageRequest = 0
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

    /** Saves connection settings. A blank key means "use Claudio Free". */
    fun saveSettings(apiKey: String, baseUrl: String, model: String) {
        viewModelScope.launch {
            val key = apiKey.trim()
            // Never send the built-in free key to an arbitrary endpoint.
            val safeBase = if (key.isBlank()) Settings.DEFAULT_BASE_URL else baseUrl.trim()
            val safeModel = model.trim().ifBlank { Settings.DEFAULT_MODEL }
            settingsStore.save(key, safeBase, safeModel)
            fetchModels(key.ifBlank { AppConfig.FREE_API_KEY }, safeBase)
        }
    }

    /** One tap back to the built-in key, default provider and a free model. */
    fun useFreeKey() {
        viewModelScope.launch {
            settingsStore.save("", Settings.DEFAULT_BASE_URL, Settings.DEFAULT_MODEL)
            fetchModels(AppConfig.FREE_API_KEY, Settings.DEFAULT_BASE_URL)
        }
    }

    fun saveTheme(theme: ThemePrefs) {
        viewModelScope.launch { settingsStore.saveTheme(theme) }
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
                    return@launch
                }
                val current = settings.value
                val shouldAutoPick = current.usingFreeKey || current.model == Settings.DEFAULT_MODEL
                if (shouldAutoPick) {
                    val keepCurrent = list.any { it.id == current.model && it.vision }
                    if (!keepCurrent) {
                        val pick = pickFreeModel(list)
                        if (pick != null) {
                            settingsStore.save(current.customKey, current.baseUrl, pick.id)
                        }
                    }
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

    private fun pickFreeModel(list: List<ModelInfo>): ModelInfo? {
        val favorites = listOf(
            "nemotron-3.5-super-vl",
            "gemma-4-31b",
            "ling-3.2",
            "gemma-4-26b",
            "gemini",
            "qwen2.5-vl",
            "llama-3.2"
        )
        favorites.forEach { name ->
            list.firstOrNull { it.vision && it.id.contains(name, ignoreCase = true) }?.let { return it }
        }
        return list.firstOrNull { it.vision } ?: list.firstOrNull()
    }

    fun loadImage(uri: Uri) {
        imageJob?.cancel()
        val request = ++imageRequest
        _pendingImage.value = null
        pendingBase64 = null
        imageJob = viewModelScope.launch {
            _loadingImage.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val bitmap = ImageUtils.loadScaled(getApplication(), uri)
                        bitmap?.let { it.asImageBitmap() to ImageUtils.toBase64(it) }
                    }.getOrNull()
                }
                if (request != imageRequest) return@launch
                if (result != null) {
                    _pendingImage.value = result.first
                    pendingBase64 = result.second
                } else {
                    _error.value = "Couldn't read that image. Try taking the photo again."
                }
            } finally {
                if (request == imageRequest) {
                    _loadingImage.value = false
                    imageJob = null
                }
            }
        }
    }

    fun clearPending() {
        imageRequest++
        imageJob?.cancel()
        imageJob = null
        _pendingImage.value = null
        _loadingImage.value = false
        pendingBase64 = null
    }

    /** Starts a session from a photo, a typed question, or both. */
    fun startSession(question: String) {
        val base64 = pendingBase64
        val trimmed = question.trim()
        if (base64 == null && trimmed.isEmpty()) return
        val session = Session(
            title = trimmed.ifBlank { "Photo question" }.take(72),
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
        val image = pendingBase64
        if ((trimmed.isEmpty() && image == null) || _streaming.value) return
        _current.value = session.copy(
            messages = session.messages + ChatMessage(role = "user", text = trimmed, imageBase64 = image),
            updatedAt = System.currentTimeMillis()
        )
        clearPending()
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

    /**
     * Streams a completion into the current session.
     *
     * Deltas are coalesced into ~20 UI updates per second. Updating Compose on
     * every token made the answer bubble visibly flicker; batching keeps the
     * text smooth and the scroll position stable.
     */
    private fun runCompletion() {
        streamJob?.cancel()
        val base = _current.value ?: return
        if (settings.value.apiKey.isBlank()) {
            _error.value = "No API key is configured in this build. Open Settings → Connection and add your own key."
            return
        }
        streamJob = viewModelScope.launch {
            _streaming.value = true
            _error.value = null
            var session = base.copy(
                messages = base.messages + ChatMessage(role = "assistant", text = ""),
                updatedAt = System.currentTimeMillis()
            )
            _current.value = session

            val reasoning = StringBuilder()
            val answer = StringBuilder()
            var reasoningStart = 0L
            var reasoningMs: Long? = null
            var lastFlush = 0L

            fun flush() {
                val now = System.currentTimeMillis()
                lastFlush = now
                val messages = session.messages.toMutableList()
                if (messages.isEmpty()) return
                val last = messages.last()
                val updated = last.copy(
                    text = answer.toString(),
                    reasoning = if (reasoning.isEmpty()) last.reasoning else reasoning.toString(),
                    reasoningMs = reasoningMs ?: last.reasoningMs
                )
                messages[messages.lastIndex] = updated
                session = session.copy(messages = messages, updatedAt = now)
                _current.value = session
            }

            try {
                OpenRouterClient.streamChat(settings.value, base.messages).collect { event ->
                    when (event) {
                        is StreamEvent.Reasoning -> {
                            if (reasoningStart == 0L) reasoningStart = System.currentTimeMillis()
                            reasoning.append(event.text)
                        }
                        is StreamEvent.Content -> {
                            if (reasoningStart != 0L && reasoningMs == null) {
                                reasoningMs = System.currentTimeMillis() - reasoningStart
                            }
                            answer.append(event.text)
                        }
                    }
                    val now = System.currentTimeMillis()
                    if (now - lastFlush >= 50L) flush()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Something went wrong. Please try again."
            } finally {
                if (reasoningStart != 0L && reasoningMs == null && reasoning.isNotEmpty()) {
                    reasoningMs = System.currentTimeMillis() - reasoningStart
                }
                flush()
                _streaming.value = false
                val last = session.messages.lastOrNull()
                if (last?.role == "assistant") {
                    if (last.text.isBlank() && last.reasoning.isNullOrBlank()) {
                        session = session.copy(messages = session.messages.dropLast(1))
                        _current.value = session
                    } else if (last.reasoningMs == null && !last.reasoning.isNullOrBlank()) {
                        val messages = session.messages.toMutableList()
                        messages[messages.lastIndex] = last.copy(reasoningMs = reasoningMs)
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
