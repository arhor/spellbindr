package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favoriteOnly: Boolean = false,
    ): List<Spell> {
        val spells = spellsRepository.findSpells(
            query = query,
            classes = classes,
        )
        if (!favoriteOnly) return spells

        val favoriteIds = favoritesRepository.observeFavoriteIds(FavoriteType.SPELL).first().toSet()

        return spells.filter { it.id in favoriteIds }
    }
}
