package com.github.arhor.spellbindr.core.common.data.repository

import com.github.arhor.spellbindr.core.common.data.model.Spell
import com.github.arhor.spellbindr.core.common.data.model.SpellcastingClass
import kotlinx.coroutines.flow.Flow

interface SpellRepository {
    fun getFavoriteSpells(): Flow<List<String>>

    fun findSpellByName(name: String): Spell?

    fun findSpells(query: String? = null, classes: Set<SpellcastingClass>? = null): List<Spell>

    fun findSpells(queries: List<String>? = null, classes: Set<SpellcastingClass>? = null): List<Spell>

    suspend fun toggleFavorite(spellName: String)

    suspend fun isFavorite(name: String?, favoriteSpells: List<String>? = null): Boolean
}
