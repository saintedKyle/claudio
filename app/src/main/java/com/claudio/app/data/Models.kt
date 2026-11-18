package com.claudio.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val imageBase64: String? = null,
    val reasoning: String? = null,
    val reasoningMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ModelInfo(
    val id: String,
    val name: String,
    val vision: Boolean
)

@Serializable
data class Session(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val model: String,
    val messages: List<ChatMessage> = emptyList()
)

data class Settings(
    val apiKey: String = AppConfig.FREE_API_KEY,
    val customKey: String = "",
    val baseUrl: String = DEFAULT_BASE_URL,
    val model: String = DEFAULT_MODEL,
    val webSearch: Boolean = false,
    val theme: ThemePrefs = ThemePrefs(),
    val loaded: Boolean = false
) {
    /** True when Claudio's built-in free key is doing the work. */
    val usingFreeKey: Boolean get() = customKey.isBlank()

    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"

        /** A free vision model that is currently available on OpenRouter. */
        const val DEFAULT_MODEL = "nvidia/nemotron-3.5-super-vl:free"

        val FREE_API_KEY: String get() = AppConfig.FREE_API_KEY
    }
}

val CURATED_MODELS = listOf(
    "nvidia/nemotron-3.5-super-vl:free" to "Nemotron 3.5 Super VL · free & sees photos",
    "google/gemma-4-31b-it:free" to "Gemma 4 31B · free & sees photos",
    "inclusionai/ling-3.2-flash:free" to "Ling 3.2 Flash · free & sees photos",
    "google/gemma-4-26b-a4b-it:free" to "Gemma 4 26B · free & sees photos",
    "google/gemini-2.5-flash" to "Gemini 2.5 Flash · fast & smart",
    "openai/gpt-4o-mini" to "GPT-4o mini · balanced",
    "anthropic/claude-3.5-sonnet" to "Claude 3.5 Sonnet · great tutor",
    "qwen/qwen2.5-vl-72b-instruct" to "Qwen 2.5 VL · handwriting pro"
)
