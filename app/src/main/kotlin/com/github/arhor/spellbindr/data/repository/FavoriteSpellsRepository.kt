package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteSpellsRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val favoriteSpellNamesFlow: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    suspend fun updateFavoriteSpells(updated: List<String>) {
        context.spellListsDataStore.edit { preferences ->
            preferences[FAVORITE_SPELLS] = json.encodeToString(updated)
        }
    }

    companion object {
        private const val DATASTORE_NAME = "favorite_spells"
        private val FAVORITE_SPELLS = stringPreferencesKey(DATASTORE_NAME)
        private val Context.spellListsDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }
} 
