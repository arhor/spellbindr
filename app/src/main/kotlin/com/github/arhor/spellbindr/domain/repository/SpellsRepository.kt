package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing spell data and managing favorite spells.
 */
interface SpellsRepository {
    /**
     * A Flow emitting the complete list of available spells.
     * These are typically loaded from static assets.
     */
    val allSpells: Flow<List<Spell>>

    /**
     * A Flow emitting the list of IDs for spells marked as favorites by the user.
     */
    val favoriteSpellIds: Flow<List<String>>

    /**
     * Retrieves a single spell by its [id].
     *
     * @return The [Spell] if found, null otherwise.
     */
    suspend fun getSpellById(id: String): Spell?

    /**
     * Toggles the favorite status of a spell.
     *
     * @param spellId The unique identifier of the spell.
     */
    suspend fun toggleFavorite(spellId: String)

    /**
     * Checks if a spell is currently a favorite.
     *
     * @param spellId The spell ID to check.
     * @param favoriteSpellIds Optional list of current favorites to check against (optimization to avoid re-fetching).
     */
    suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>? = null): Boolean
}
