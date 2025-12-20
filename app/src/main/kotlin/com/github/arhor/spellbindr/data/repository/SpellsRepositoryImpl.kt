package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.FavoriteSpellsDataStore
import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.data.model.Spell as DataSpell
import com.github.arhor.spellbindr.data.model.toDomain
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsRepositoryImpl @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
    private val favoriteSpellsDataStore: FavoriteSpellsDataStore,
) : SpellsRepository {
    override val allSpells: Flow<List<Spell>>
        get() = allSpellsDataStore.data.map { spells ->
            spells?.map { it.toDomain() } ?: emptyList()
        }

    override val favoriteSpellIds: Flow<List<String>>
        get() = favoriteSpellsDataStore.data.map { it ?: emptyList() }

    override suspend fun getSpellById(id: String): Spell? =
        allSpellsDataStore.data.firstOrNull()?.firstOrNull { it.id == id }?.toDomain()

    override suspend fun findSpells(
        query: String,
        classes: Set<EntityRef>,
        favoriteOnly: Boolean,
    ): List<Spell> =
        allSpellsDataStore.data.firstOrNull()?.let { spells ->
            if (favoriteOnly) {
                val favorites = favoriteSpellIds.first()

                spells.filter { it.shouldBeIncluded(query, classes) && it.id in favorites }
            } else {
                spells.filter { it.shouldBeIncluded(query, classes) }
            }
        }?.map { it.toDomain() } ?: emptyList()

    override suspend fun toggleFavorite(spellId: String) {
        favoriteSpellIds.first()
            .let { if (spellId in it) it - spellId else it + spellId }
            .let { favoriteSpellsDataStore.store(it) }
    }

    override suspend fun isFavorite(
        spellId: String?,
        favoriteSpellIds: List<String>?,
    ): Boolean {
        val targetSpellId = spellId ?: return false
        val ids = favoriteSpellIds ?: this.favoriteSpellIds.first()

        return targetSpellId in ids
    }

    private fun DataSpell.shouldBeIncluded(
        query: String,
        classes: Set<EntityRef>,
    ): Boolean {
        return (query.isBlank() || query.let { this.name.contains(it, ignoreCase = true) })
            && (classes.isEmpty() || classes.all { this.classes.contains(it) })
    }
}
