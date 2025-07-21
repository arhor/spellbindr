package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteSpellsDataStore @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val json: Json,
) {
    val data: Flow<List<String>?>
        get() = context.spellListsDataStore.data.map { it.extract() }

    private fun Preferences.extract(): List<String>? = this[FAVORITE_SPELLS]
        ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
        ?.getOrNull()

    suspend fun store(favoriteSpellIds: List<String>) {
        val string = withContext(Dispatchers.Default) {
            json.encodeToString(favoriteSpellIds)
        }
        context.spellListsDataStore.edit {
            it[FAVORITE_SPELLS] = string
        }
    }

    companion object {
        private const val DATASTORE_NAME = "favorite_spells"
        private val FAVORITE_SPELLS = stringPreferencesKey(DATASTORE_NAME)
        private val Context.spellListsDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }
}
