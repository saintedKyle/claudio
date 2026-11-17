package com.claudio.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class HistoryStore(context: Context) {

    private val dir = File(context.filesDir, "sessions").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun save(session: Session) = withContext(Dispatchers.IO) {
        runCatching {
            File(dir, "${session.id}.json").writeText(json.encodeToString(Session.serializer(), session))
        }
    }

    suspend fun loadAll(): List<Session> = withContext(Dispatchers.IO) {
        dir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { f -> runCatching { json.decodeFromString(Session.serializer(), f.readText()) }.getOrNull() }
            ?.sortedByDescending { it.updatedAt }
            ?: emptyList()
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        runCatching { File(dir, "$id.json").delete() }
    }
}