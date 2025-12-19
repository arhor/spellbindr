package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.coroutines.flow.Flow

interface SpellsRepository {
    val allSpells: Flow<List<Spell>>

    suspend fun getSpellById(id: String): Spell?

    suspend fun findSpells(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
    ): List<Spell>
}
