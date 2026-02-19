package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.SearchAndGroupSpellsResult
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.domain.usecase.internal.matchesFilters
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchAndGroupSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(
        query: String = "",
        classes: List<EntityRef> = emptyList(),
        favoriteOnly: Boolean = false,
        allSpells: List<Spell>? = null,
        favoriteSpellIds: Set<String>? = null,
    ): SearchAndGroupSpellsResult {
        val resolvedSpells = allSpells ?: when (
            val spellsState = spellsRepository.allSpellsState.first { it !is Loadable.Loading }
        ) {
            is Loadable.Content -> spellsState.data
            is Loadable.Failure -> throw (spellsState.cause ?: IllegalStateException(
                spellsState.errorMessage ?: "Failed to load spells."
            ))

            is Loadable.Loading -> emptyList()
        }
        val favoriteIds = if (favoriteOnly) {
            favoriteSpellIds ?: favoritesRepository.observeFavoriteIds(FavoriteType.SPELL).first().toSet()
        } else {
            emptySet()
        }
        val filteredSpells = resolvedSpells.filter { spell ->
            val favoritesFilter = if (favoriteOnly) favoriteIds else emptySet()
            spell.matchesFilters(query = query, classes = classes, favoriteSpellIds = favoritesFilter)
        }

        return SearchAndGroupSpellsResult(
            spells = filteredSpells,
            totalCount = filteredSpells.size,
            query = query.trim(),
            classes = classes,
            favoriteOnly = favoriteOnly,
        )
    }
}
