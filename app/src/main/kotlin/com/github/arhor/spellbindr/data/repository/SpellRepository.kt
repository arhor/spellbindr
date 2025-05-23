package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import com.github.arhor.spellbindr.data.model.StaticAsset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellRepository @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val json: Json,
) : DataLoader {

    private lateinit var data: List<Spell>
    private val mutex = Mutex()

    val favoriteSpells: Flow<List<String>> = context.spellListsDataStore.data.map {
        it[FAVORITE_SPELLS]
            ?.let { runCatching { json.decodeFromString<List<String>>(it) } }
            ?.getOrNull()
            ?: emptyList()
    }

    override val resource: String
        get() = "Spells"

    override suspend fun loadData() {
        if (!::data.isInitialized) {
            mutex.withLock {
                if (!::data.isInitialized) {
                    data = withContext(Dispatchers.IO) {
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

    suspend fun findSpellByName(name: String): Spell? = spells().find { it.name == name }

    suspend fun findSpells(
        query: String = "",
        classes: Set<SpellcastingClass> = emptySet(),
        favorite: Boolean = false,
    ): List<Spell> =
        spells().let {
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

    private suspend fun spells(): List<Spell> {
        loadData()
        return data
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
