package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsRepositoryImpl @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
) : SpellsRepository {
    override val allSpells: Flow<List<Spell>>
        get() = allSpellsDataStore.data.map { it ?: emptyList() }

    override suspend fun getSpellById(id: String): Spell? =
        allSpells.firstOrNull()?.find { it.id == id }

    override suspend fun findSpells(
        query: String,
        classes: Set<EntityRef>,
    ): List<Spell> =
        allSpells.firstOrNull()?.let { spells ->
            spells.filter { it.shouldBeIncluded(query, classes) }
        } ?: emptyList()

    private fun Spell.shouldBeIncluded(
        query: String,
        classes: Set<EntityRef>,
    ): Boolean {
        return (query.isBlank() || query.let { this.name.contains(it, ignoreCase = true) })
            && (classes.isEmpty() || classes.all { this.classes.contains(it) })
    }
}
