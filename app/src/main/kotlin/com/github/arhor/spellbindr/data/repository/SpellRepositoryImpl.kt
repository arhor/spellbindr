package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.core.common.data.model.Spell
import com.github.arhor.spellbindr.core.common.data.model.SpellcastingClass
import com.github.arhor.spellbindr.core.common.data.repository.SpellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SpellRepositoryImpl @Inject constructor(
    private val context: Context,
    private val json: Json,
) : SpellRepository {
    private val spells by lazy {
        context.assets.open("spells/data.json")
            .bufferedReader()
            .use { it.readText() }
            .let { json.decodeFromString<List<Spell>>(it) }
            .sortedWith(compareBy<Spell> { it.level }.thenBy { it.name })
    }
    private val favoriteSpellsFlow: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    override fun getFavoriteSpells(): Flow<List<String>> = favoriteSpellsFlow

    override fun findSpellByName(name: String): Spell? = spells.find { it.name == name }

    override fun findSpells(
        query: String?,
        classes: Set<SpellcastingClass>?,
    ): List<Spell> = findSpells(queries = query?.takeIf(String::isNotBlank)?.let(::listOf), classes = classes)

    override fun findSpells(
        queries: List<String>?,
        classes: Set<SpellcastingClass>?,
    ): List<Spell> = spells.filter { it.shouldBeIncluded(queries, classes) }

    override suspend fun toggleFavorite(spellName: String) {
        val updatedFavoriteSpells =
            favoriteSpellsFlow.first()
                .let { if (spellName in it) it - spellName else it + spellName }
                .let { json.encodeToString(it) }

        context.spellListsDataStore.edit { preferences ->
            preferences[FAVORITE_SPELLS] = updatedFavoriteSpells
        }
    }

    override suspend fun isFavorite(name: String?, favoriteSpells: List<String>?): Boolean {
        val spellName = name ?: return false
        val favorites = favoriteSpells ?: favoriteSpellsFlow.first()

        return spellName in favorites
    }

    private fun Spell.shouldBeIncluded(
        queries: List<String>?,
        classes: Set<SpellcastingClass>?
    ): Boolean {
        return (queries.isNullOrEmpty() || queries.any { this.name.contains(it, ignoreCase = true) })
            && (classes.isNullOrEmpty() || classes.all { this.classes.contains(it) })
    }

    companion object {
        private const val DATASTORE_NAME = "favorite_spells"
        private val FAVORITE_SPELLS = stringPreferencesKey(DATASTORE_NAME)
        private val Context.spellListsDataStore by preferencesDataStore(name = DATASTORE_NAME)
    }
}
