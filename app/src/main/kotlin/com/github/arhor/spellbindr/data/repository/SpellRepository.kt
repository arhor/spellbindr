package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.common.EntityRef
import com.github.arhor.spellbindr.data.local.assets.FavoriteSpellsDataStore
import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.data.model.Spell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellRepository @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
    private val favSpellsDataStore: FavoriteSpellsDataStore,
) {
    val allSpells: Flow<List<Spell>>
        get() = allSpellsDataStore.data.map { it ?: emptyList() }

    val favSpells: Flow<List<String>>
        get() = favSpellsDataStore.data.map { it ?: emptyList() }

    suspend fun findSpellByName(name: String): Spell? = allSpells.firstOrNull()?.find { it.name == name }

    suspend fun findSpells(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favorite: Boolean = false,
    ): List<Spell> =
        allSpells.firstOrNull()?.let { spells ->
            if (favorite) {
                val favorites = favSpells.first()

                spells.filter { it.shouldBeIncluded(query, classes) && it.id in favorites }
            } else {
                spells.filter { it.shouldBeIncluded(query, classes) }
            }
        } ?: emptyList()

    suspend fun toggleFavorite(spellId: String) {
        favSpells.first()
            .let { if (spellId in it) it - spellId else it + spellId }
            .let { favSpellsDataStore.store(it) }
    }

    suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>? = null): Boolean {
        val targetSpellId = spellId ?: return false
        val favorSpellIds = favoriteSpellIds ?: favSpells.first()

        return targetSpellId in favorSpellIds
    }

    private fun Spell.shouldBeIncluded(
        query: String,
        classes: Set<EntityRef>,
    ): Boolean {
        return (query.isBlank() || query.let { this.name.contains(it, ignoreCase = true) })
            && (classes.isEmpty() || classes.all { this.classes.contains(it) })
    }
}
