package com.beardydev.lookhere.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val Context.dataStore by preferencesDataStore(name = "look_here_settings")

private const val MAX_RECENT_GIFS = 12

class SelectedGifRepository(private val context: Context) {

    private object Keys {
        val SOURCE = stringPreferencesKey("gif_source")
        val ID = stringPreferencesKey("gif_id")
        val FULL_URL = stringPreferencesKey("gif_full_url")
        val PREVIEW_URL = stringPreferencesKey("gif_preview_url")
        val DESCRIPTION = stringPreferencesKey("gif_description")
        val FILE_PATH = stringPreferencesKey("gif_file_path")
        val CUSTOMER_ID = stringPreferencesKey("customer_id")
        val RECENT_GIFS_JSON = stringPreferencesKey("recent_gifs_json")
    }

    val selectedGif: Flow<SelectedGif?> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.SOURCE]) {
            "search" -> {
                val id = prefs[Keys.ID]
                val fullUrl = prefs[Keys.FULL_URL]
                val previewUrl = prefs[Keys.PREVIEW_URL]
                val description = prefs[Keys.DESCRIPTION]
                if (id != null && fullUrl != null && previewUrl != null && description != null) {
                    SelectedGif.FromSearch(id, fullUrl, previewUrl, description)
                } else {
                    null
                }
            }
            "device" -> prefs[Keys.FILE_PATH]?.let { SelectedGif.FromDevice(it) }
            else -> null
        }
    }

    val recentGifs: Flow<List<SelectedGif>> = context.dataStore.data.map { prefs ->
        prefs[Keys.RECENT_GIFS_JSON]?.let { json -> decodeRecents(json) } ?: emptyList()
    }

    suspend fun save(gif: SelectedGif) {
        context.dataStore.edit { prefs ->
            when (gif) {
                is SelectedGif.FromSearch -> {
                    prefs[Keys.SOURCE] = "search"
                    prefs[Keys.ID] = gif.id
                    prefs[Keys.FULL_URL] = gif.fullUrl
                    prefs[Keys.PREVIEW_URL] = gif.previewUrl
                    prefs[Keys.DESCRIPTION] = gif.description
                    prefs.remove(Keys.FILE_PATH)
                }
                is SelectedGif.FromDevice -> {
                    prefs[Keys.SOURCE] = "device"
                    prefs[Keys.FILE_PATH] = gif.filePath
                    prefs.remove(Keys.ID)
                    prefs.remove(Keys.FULL_URL)
                    prefs.remove(Keys.PREVIEW_URL)
                    prefs.remove(Keys.DESCRIPTION)
                }
            }

            val existingRecents = prefs[Keys.RECENT_GIFS_JSON]?.let { decodeRecents(it) } ?: emptyList()
            val updatedRecents = (listOf(gif) + existingRecents.filterNot { it.stableKey == gif.stableKey })
                .take(MAX_RECENT_GIFS)
            prefs[Keys.RECENT_GIFS_JSON] = Json.encodeToString(updatedRecents)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SOURCE)
            prefs.remove(Keys.ID)
            prefs.remove(Keys.FULL_URL)
            prefs.remove(Keys.PREVIEW_URL)
            prefs.remove(Keys.DESCRIPTION)
            prefs.remove(Keys.FILE_PATH)
        }
    }

    suspend fun getOrCreateCustomerId(): String {
        val existing = context.dataStore.data.first()[Keys.CUSTOMER_ID]
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { prefs -> prefs[Keys.CUSTOMER_ID] = generated }
        return generated
    }

    private fun decodeRecents(json: String): List<SelectedGif> =
        runCatching { Json.decodeFromString<List<SelectedGif>>(json) }.getOrDefault(emptyList())
}
