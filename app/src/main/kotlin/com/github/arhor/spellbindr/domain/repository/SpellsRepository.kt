package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.coroutines.flow.Flow

interface SpellsRepository {
    val allSpells: Flow<List<Spell>>
    val favoriteSpellIds: Flow<List<String>>

    suspend fun getSpellById(id: String): Spell?

    suspend fun toggleFavorite(spellId: String)

    suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>? = null): Boolean
}
