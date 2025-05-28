package com.github.arhor.spellbindr.data.next.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.data.next.model.EntityRef
import com.github.arhor.spellbindr.data.next.model.Spell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellRepository @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetLoaderBase<Spell>(
    context = context,
    json = json,
    path = "data/spells.json",
    serializer = Spell.serializer()
) {

    val favoriteSpells: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    suspend fun findSpellByName(name: String): Spell? = getAsset().find { it.name == name }

    suspend fun findSpells(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favorite: Boolean = false,
    ): List<Spell> =
        getAsset().let {
            if (favorite) {
                val favorites = favoriteSpells.first()

                it.filter { it.shouldBeIncluded(query, classes) && it.name in favorites }
            } else {
                it.filter { it.shouldBeIncluded(query, classes) }
            }
        }

    suspend fun toggleFavorite(spellName: String) {
        val updatedFavoriteSpells =
            favoriteSpells.first()
                .let { if (spellName in it) it - spellName else it + spellName }
                .let { json.encodeToString(it) }

        context.spellListsDataStore.edit { preferences ->
            preferences[FAVORITE_SPELLS] = updatedFavoriteSpells
        }
    }

    suspend fun isFavorite(name: String?, favoriteSpells: List<String>? = null): Boolean {
        val spellName = name ?: return false
        val favorites = favoriteSpells ?: this.favoriteSpells.first()

        return spellName in favorites
    }

    private fun Spell.shouldBeIncluded(
        query: String,
        classes: Set<EntityRef>,
    ): Boolean {
        return (query.isBlank() || query.let { this.name.contains(it, ignoreCase = true) })
            && (classes.isEmpty() || classes.all { this.classes.contains(it) })
    }

    companion object {
        private const val DATASTORE_NAME = "favorite_spells"
        private val FAVORITE_SPELLS = stringPreferencesKey(DATASTORE_NAME)
        private val Context.spellListsDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }
}
