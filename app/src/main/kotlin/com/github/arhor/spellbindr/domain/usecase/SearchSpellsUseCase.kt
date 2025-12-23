package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import javax.inject.Inject

/**
 * Use case for searching and filtering the global spell list.
 *
 * This implementation delegates to [SearchAndGroupSpellsUseCase] but returns a flat list
 * instead of grouped results.
 */
class SearchSpellsUseCase @Inject constructor(
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
) {
    /**
     * Searches for spells matching the criteria.
     *
     * @param query Text search query (matches against name).
     * @param classes Set of class references to filter by.
     * @param favoriteOnly If true, returns only favorite spells.
     * @return List of matching [Spell] objects.
     */
    suspend operator fun invoke(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favoriteOnly: Boolean = false,
    ): List<Spell> {
        return searchAndGroupSpellsUseCase(
            query = query,
            classes = classes,
            favoriteOnly = favoriteOnly,
        ).spells
    }
}
