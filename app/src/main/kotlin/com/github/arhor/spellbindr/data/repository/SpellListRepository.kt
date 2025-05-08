package com.github.arhor.spellbindr.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.arhor.spellbindr.data.model.SpellList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val SPELL_LISTS_DATASTORE_NAME = "spell_lists"
private val Context.spellListsDataStore by preferencesDataStore(name = SPELL_LISTS_DATASTORE_NAME)
private val SPELL_LISTS_KEY = stringPreferencesKey("spell_lists")

@Singleton
class SpellListRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val spellListsFlow: Flow<List<SpellList>> = context.spellListsDataStore.data
        .map { preferences ->
            preferences[SPELL_LISTS_KEY]?.let {
                runCatching { json.decodeFromString<List<SpellList>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
        }

    suspend fun setSpellLists(lists: List<SpellList>) {
        context.spellListsDataStore.edit { preferences ->
            preferences[SPELL_LISTS_KEY] = json.encodeToString(lists)
        }
    }

    suspend fun addSpellList(list: SpellList) {
        val current = spellListsFlow.map { it.toMutableList() }.first()
        current.add(list)
        setSpellLists(current)
    }

    suspend fun updateSpellList(updated: SpellList) {
        val current = spellListsFlow.map { lists ->
            lists.map { if (it.name == updated.name) updated else it }
        }.first()
        setSpellLists(current)
    }

    suspend fun deleteSpellList(name: String) {
        val current = spellListsFlow.map { lists ->
            lists.filterNot { it.name == name }
        }.first()
        setSpellLists(current)
    }
} 