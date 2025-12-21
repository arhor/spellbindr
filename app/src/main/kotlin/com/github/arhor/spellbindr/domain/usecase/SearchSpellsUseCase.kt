package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import javax.inject.Inject

class SearchSpellsUseCase @Inject constructor(
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
) {
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
