package com.claudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "claudio_settings")

class SettingsStore(private val context: Context) {

    private val keyApi = stringPreferencesKey("api_key")
    private val keyBase = stringPreferencesKey("base_url")
    private val keyModel = stringPreferencesKey("model")

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            apiKey = prefs[keyApi] ?: "",
            baseUrl = prefs[keyBase]?.ifBlank { Settings.DEFAULT_BASE_URL } ?: Settings.DEFAULT_BASE_URL,
            model = prefs[keyModel]?.ifBlank { Settings.DEFAULT_MODEL } ?: Settings.DEFAULT_MODEL,
            loaded = true
        )
    }

    suspend fun save(apiKey: String, baseUrl: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[keyApi] = apiKey.trim()
            prefs[keyBase] = baseUrl.trim().trimEnd('/').ifBlank { Settings.DEFAULT_BASE_URL }
            prefs[keyModel] = model.trim().ifBlank { Settings.DEFAULT_MODEL }
        }
    }
}