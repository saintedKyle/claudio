package com.claudio.app.net

import com.claudio.app.data.ChatMessage
import com.claudio.app.data.Settings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiError(message: String) : IOException(message)

object OpenRouterClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private const val SYSTEM_PROMPT =
        "You are Claudio, a warm, brilliant study tutor for students. The user sends a photo of a question, " +
        "problem, notes, diagram, drawing or textbook page, possibly with their own question about it. " +
        "First read everything visible in the image carefully (including handwriting). Then answer the user's request: " +
        "solve problems step by step, explain concepts clearly, summarize, translate, or whatever they ask. " +
        "Use markdown: short headings, numbered steps, **bold** for key results, and `code` where relevant. " +
        "Be encouraging and concise but complete. If part of the image is unreadable, say so honestly and answer what you can. " +
        "Always end problem solutions with the final answer clearly stated."

    fun streamChat(settings: Settings, history: List<ChatMessage>): Flow<String> = callbackFlow {
        val body = buildRequestBody(settings, history)
        val request = Request.Builder()
            .url(settings.baseUrl.trimEnd('/') + "/chat/completions")
            .header("Authorization", "Bearer ${settings.apiKey}")
            .header("HTTP-Referer", "https://github.com/saintedKyle/claudio")
            .header("X-Title", "Claudio")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(ApiError("Network error: ${e.message ?: "could not reach the AI provider"}. Check your connection and base URL."))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        close(ApiError(parseHttpError(res.code, res.body?.string())))
                        return
                    }
                    try {
                        val source = res.body?.source() ?: throw IOException("Empty response from provider")
                        while (true) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data == "[DONE]") break
                            val delta = extractDelta(data)
                            if (delta != null) trySend(delta)
                        }
                        close()
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        })
        awaitClose { call.cancel() }
    }.buffer(Channel.UNLIMITED)

    private fun buildRequestBody(settings: Settings, history: List<ChatMessage>): JsonObject =
        buildJsonObject {
            put("model", settings.model)
            put("stream", true)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                history.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role)
                        if (msg.role == "user" && msg.imageBase64 != null) {
                            putJsonArray("content") {
                                add(buildJsonObject {
                                    put("type", "text")
                                    put("text", msg.text.ifBlank { "Please look at this image and help me with it." })
                                })
                                add(buildJsonObject {
                                    put("type", "image_url")
                                    putJsonObject("image_url") {
                                        put("url", "data:image/jpeg;base64,${msg.imageBase64}")
                                    }
                                })
                            }
                        } else {
                            put("content", msg.text)
                        }
                    })
                }
            }
        }

    private fun extractDelta(data: String): String? {
        val obj = runCatching { json.parseToJsonElement(data).jsonObject }.getOrNull() ?: return null
        obj["error"]?.let { err ->
            val msg = runCatching { err.jsonObject["message"]?.jsonPrimitive?.contentOrNull }.getOrNull()
            throw ApiError(msg ?: "The provider reported an error mid-answer.")
        }
        val choices = obj["choices"] as? JsonArray ?: return null
        val delta = choices.firstOrNull()?.jsonObject?.get("delta")?.jsonObject ?: return null
        return runCatching { delta["content"]?.jsonPrimitive?.contentOrNull }.getOrNull()
    }

    private fun parseHttpError(code: Int, body: String?): String {
        val providerMsg = runCatching {
            json.parseToJsonElement(body ?: "").jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        val hint = when (code) {
            401 -> "Your API key was rejected. Double-check it in Settings."
            402 -> "Your account is out of credits. Top up at openrouter.ai or pick a cheaper model."
            404 -> "Model not found. Pick a different model in Settings."
            429 -> "Rate limited. Wait a moment and try again."
            else -> "The provider returned an error."
        }
        return "$hint (HTTP $code)${providerMsg?.let { "\n$it" } ?: ""}"
    }
}