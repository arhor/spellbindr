package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import com.github.arhor.spellbindr.data.model.StaticAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SpellRepository @Inject constructor(
    private val context: Context,
    private val json: Json,
) {
    private val spells by lazy {
        context.assets.open("spells/data.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<StaticAsset<Spell, Unit>>(it).data }
            .sortedWith(compareBy<Spell> { it.level }.thenBy { it.name })
    }
    val favoriteSpells: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    fun findSpellByName(name: String): Spell? = spells.find { it.name == name }

    suspend fun findSpells(query: String, classes: Set<SpellcastingClass>, favorite: Boolean): List<Spell> =
        if (favorite) {
            val favorites = favoriteSpells.first()

            spells.filter { it.shouldBeIncluded(query, classes) && it.name in favorites }
        } else {
            spells.filter { it.shouldBeIncluded(query, classes) }
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
