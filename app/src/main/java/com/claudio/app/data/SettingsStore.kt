package com.claudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "claudio_settings")

class SettingsStore(private val context: Context) {

    private val keyApi = stringPreferencesKey("api_key")
    private val keyBase = stringPreferencesKey("base_url")
    private val keyModel = stringPreferencesKey("model")
    private val keyWebSearch = booleanPreferencesKey("web_search")
    private val keyAccent = stringPreferencesKey("theme_accent")
    private val keyTone = stringPreferencesKey("theme_tone")
    private val keyBubble = stringPreferencesKey("theme_bubble")
    private val keyFontScale = floatPreferencesKey("theme_font_scale")
    private val keyReduceMotion = booleanPreferencesKey("theme_reduce_motion")
    private val keyName = stringPreferencesKey("display_name")

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        val customKey = prefs[keyApi] ?: ""
        Settings(
            apiKey = customKey.ifBlank { AppConfig.FREE_API_KEY },
            customKey = customKey,
            baseUrl = prefs[keyBase]?.ifBlank { Settings.DEFAULT_BASE_URL } ?: Settings.DEFAULT_BASE_URL,
            model = prefs[keyModel]?.ifBlank { Settings.DEFAULT_MODEL } ?: Settings.DEFAULT_MODEL,
            webSearch = prefs[keyWebSearch] ?: false,
            theme = ThemePrefs(
                accent = AccentTone.from(prefs[keyAccent]),
                tone = SurfaceTone.from(prefs[keyTone]),
                bubble = BubbleStyle.from(prefs[keyBubble]),
                fontScale = (prefs[keyFontScale] ?: 1f).coerceIn(0.9f, 1.15f),
                reduceMotion = prefs[keyReduceMotion] ?: false,
                name = prefs[keyName]?.ifBlank { AppConfig.DEFAULT_NAME } ?: AppConfig.DEFAULT_NAME
            ),
            loaded = true
        )
    }

    /** Stores the user's own key. Blank means "use the built-in free key". */
    suspend fun save(apiKey: String, baseUrl: String, model: String) {
        val trimmedKey = apiKey.trim().let { if (it == AppConfig.FREE_API_KEY) "" else it }
        context.dataStore.edit { prefs ->
            prefs[keyApi] = trimmedKey
            prefs[keyBase] = baseUrl.trim().trimEnd('/').ifBlank { Settings.DEFAULT_BASE_URL }
            prefs[keyModel] = model.trim().ifBlank { Settings.DEFAULT_MODEL }
        }
    }

    suspend fun saveWebSearch(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[keyWebSearch] = enabled }
    }

    suspend fun saveTheme(theme: ThemePrefs) {
        context.dataStore.edit { prefs ->
            prefs[keyAccent] = theme.accent.key
            prefs[keyTone] = theme.tone.key
            prefs[keyBubble] = theme.bubble.key
            prefs[keyFontScale] = theme.fontScale
            prefs[keyReduceMotion] = theme.reduceMotion
            prefs[keyName] = theme.name.trim().ifBlank { AppConfig.DEFAULT_NAME }
        }
    }
}
