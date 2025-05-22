package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import com.github.arhor.spellbindr.data.model.StaticAsset
import com.github.arhor.spellbindr.util.filterOrEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SpellRepository @Inject constructor(
    private val context: Context,
    private val json: Json,
) {
    private val spells = MutableStateFlow<List<Spell>?>(null)
    private val mutex = Mutex()

    suspend fun loadDataIfNeeded() {
        if (spells.value == null) {
            mutex.withLock {
                if (spells.value == null) {
                    spells.value = withContext(Dispatchers.IO) {
                        context.assets.open("spells/data.json")
                            .bufferedReader()
                            .use { it.readText() }
                            .let { json.decodeFromString<StaticAsset<Spell, Unit>>(it).data }
                            .sortedWith(compareBy<Spell> { it.level }.thenBy { it.name })
                    }
                }
            }
        }
    }

    val favoriteSpells: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    fun findSpellByName(name: String): Flow<Spell?> =
        spells.map { spellList ->
            spellList?.find { spell ->
                spell.name == name
            }
        }

    fun findSpells(query: String, classes: Set<SpellcastingClass>, favorite: Boolean): Flow<List<Spell>> =
        if (favorite) {
            combine(spells, favoriteSpells) { spellList, favorites ->
                spellList.filterOrEmpty { spell ->
                    spell.shouldBeIncluded(query, classes) && spell.name in favorites
                }
            }
        } else {
            spells.map { spellList ->
                spellList.filterOrEmpty { spell ->
                    spell.shouldBeIncluded(query, classes)
                }
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
        classes: Set<SpellcastingClass>,
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
