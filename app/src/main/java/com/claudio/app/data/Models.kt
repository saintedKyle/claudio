package com.claudio.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val imageBase64: String? = null,
    val timestamp: Long = System.currentTimeMillis()
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
    val apiKey: String = "",
    val baseUrl: String = DEFAULT_BASE_URL,
    val model: String = DEFAULT_MODEL,
    val loaded: Boolean = false
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        const val DEFAULT_MODEL = "google/gemini-2.0-flash-001"
    }
}

val CURATED_MODELS = listOf(
    "google/gemini-2.0-flash-001" to "Gemini 2.0 Flash · fast & cheap",
    "google/gemini-2.5-flash" to "Gemini 2.5 Flash · smarter",
    "openai/gpt-4o-mini" to "GPT-4o mini · balanced",
    "openai/gpt-4o" to "GPT-4o · strongest OpenAI",
    "anthropic/claude-3.5-sonnet" to "Claude 3.5 Sonnet · great tutor",
    "qwen/qwen2.5-vl-72b-instruct" to "Qwen 2.5 VL · handwriting pro",
    "meta-llama/llama-3.2-11b-vision-instruct" to "Llama 3.2 Vision · budget"
)