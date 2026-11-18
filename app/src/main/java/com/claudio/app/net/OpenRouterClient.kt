package com.claudio.app.net

import com.claudio.app.data.ChatMessage
import com.claudio.app.data.ModelInfo
import com.claudio.app.data.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
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

sealed interface StreamEvent {
    data class Reasoning(val text: String) : StreamEvent
    data class Content(val text: String) : StreamEvent
}

object OpenRouterClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private const val SYSTEM_PROMPT =
        "You are Claudio, a warm, brilliant study tutor for students. The user may send a photo of a question, " +
        "problem, notes, diagram, drawing or textbook page — sometimes with their own question about it — or simply " +
        "type a question with no photo. If an image is present, first read everything visible in it carefully " +
        "(including handwriting). Then answer the user's request: " +
        "solve problems step by step, explain concepts clearly, summarize, translate, or whatever they ask. " +
        "Use markdown: short headings, numbered steps, **bold** for key results, and `code` where relevant. " +
        "MATH FORMATTING: never use LaTeX, dollar signs, \\( \\) or \\[ \\]. Write every formula in plain Unicode " +
        "text that reads naturally: θ, π, °, ², ³, √, ≤, ≥, ≠, ≈, ×, ·, fractions like 3/2 or (x+2)/(x-1). " +
        "Put key results on their own line so they stand out. " +
        "DRAWING: whenever the user asks you to draw, sketch, illustrate or label something (an angle, a triangle, a " +
        "graph, a force diagram, a geometry figure, a picture of what the photo shows), output a complete standalone " +
        "SVG inside a fenced code block labelled svg. Never paste SVG markup inline in a paragraph. Rules for the SVG: start with <svg xmlns=\"http://www.w3.org/2000/svg\" " +
        "viewBox=\"0 0 400 300\"> and include a light background rect fill=\"#fdfbf3\"; draw with clear strokes " +
        "(stroke=\"#1b2a4a\" stroke-width=\"2.5\"), accent colors #2456a6, #d64545, #e8a33d; label every part with " +
        "<text> in font-family=\"sans-serif\" font-size=\"14\" fill=\"#1b2a4a\"; make angles, lengths and proportions " +
        "mathematically correct and consistent with the photo or question (e.g. a 30 degree angle must look like 30 degrees). " +
        "Close the svg tag properly. After the drawing, explain it in one or two sentences. " +
        "Be encouraging and concise but complete. If part of the image is unreadable, say so honestly and answer what you can. " +
        "Always end problem solutions with the final answer clearly stated."

    private fun supportsReasoning(model: String): Boolean {
        val m = model.lowercase()
        return listOf(
            "r1", "reason", "think", "qwq", "gemini-2.5", "gemini-3", "gpt-5",
            "o1", "o3", "o4", "magistral", "sonar-reasoning", "deepseek-v3.1", "deepseek-v3.2"
        ).any { m.contains(it) }
    }

    suspend fun fetchFreeModels(apiKey: String, baseUrl: String): List<ModelInfo> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/models")
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://github.com/saintedKyle/claudio")
            .header("X-Title", "Claudio")
            .get()
            .build()
        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) throw ApiError(parseHttpError(res.code, res.body?.string()))
            val root = json.parseToJsonElement(res.body?.string() ?: "{}").jsonObject
            val data = root["data"] as? JsonArray ?: return@use emptyList()
            data.mapNotNull { el ->
                runCatching {
                    val o = el.jsonObject
                    val id = o["id"]?.jsonPrimitive?.contentOrNull ?: return@runCatching null
                    val pricing = o["pricing"]?.jsonObject
                    val promptCost = pricing?.get("prompt")?.jsonPrimitive?.contentOrNull ?: "1"
                    val completionCost = pricing?.get("completion")?.jsonPrimitive?.contentOrNull ?: "1"
                    val isFree = id.endsWith(":free") || (promptCost == "0" && completionCost == "0")
                    if (!isFree) return@runCatching null
                    val arch = o["architecture"]?.jsonObject
                    val inputs = arch?.get("input_modalities")?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    val modality = arch?.get("modality")?.jsonPrimitive?.contentOrNull ?: ""
                    val vision = inputs.contains("image") || modality.contains("image")
                    ModelInfo(id, o["name"]?.jsonPrimitive?.contentOrNull ?: id, vision)
                }.getOrNull()
            }.sortedWith(compareByDescending<ModelInfo> { it.vision }.thenBy { it.id })
        }
    }

    fun streamChat(settings: Settings, history: List<ChatMessage>): Flow<StreamEvent> = callbackFlow {
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
                            val event = extractEvent(data)
                            if (event != null) trySend(event)
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
            if (supportsReasoning(settings.model)) {
                putJsonObject("reasoning") { put("enabled", true) }
            }
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

    private fun extractEvent(data: String): StreamEvent? {
        val obj = runCatching { json.parseToJsonElement(data).jsonObject }.getOrNull() ?: return null
        obj["error"]?.let { err ->
            val msg = runCatching { err.jsonObject["message"]?.jsonPrimitive?.contentOrNull }.getOrNull()
            throw ApiError(msg ?: "The provider reported an error mid-answer.")
        }
        val choices = obj["choices"] as? JsonArray ?: return null
        val delta = choices.firstOrNull()?.jsonObject?.get("delta")?.jsonObject ?: return null
        val reasoning = runCatching { delta["reasoning"]?.jsonPrimitive?.contentOrNull }.getOrNull()
        if (!reasoning.isNullOrEmpty()) return StreamEvent.Reasoning(reasoning)
        val content = runCatching { delta["content"]?.jsonPrimitive?.contentOrNull }.getOrNull()
        if (!content.isNullOrEmpty()) return StreamEvent.Content(content)
        return null
    }

    private fun parseHttpError(code: Int, body: String?): String {
        val providerMsg = runCatching {
            json.parseToJsonElement(body ?: "").jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        val hint = when (code) {
            401 -> "Your API key was rejected. Double-check it in Settings."
            402 -> "Your account is out of credits. Pick a free model in Settings — Claudio lists them automatically."
            404 -> "Model not found. Pick a different model in Settings."
            429 -> "Rate limited. Wait a moment and try again."
            else -> "The provider returned an error."
        }
        return "$hint (HTTP $code)${providerMsg?.let { "\n$it" } ?: ""}"
    }
}