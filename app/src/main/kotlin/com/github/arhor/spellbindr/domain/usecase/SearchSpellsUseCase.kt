package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import javax.inject.Inject

class SearchSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    suspend operator fun invoke(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favoriteOnly: Boolean = false,
    ): List<Spell> = spellsRepository.findSpells(
        query = query,
        classes = classes,
        favoriteOnly = favoriteOnly,
    )
}
