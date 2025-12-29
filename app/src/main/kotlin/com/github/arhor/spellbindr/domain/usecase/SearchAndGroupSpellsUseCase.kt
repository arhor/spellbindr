package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.SearchAndGroupSpellsResult
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchAndGroupSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(
        query: String = "",
        classes: Set<EntityRef> = emptySet(),
        favoriteOnly: Boolean = false,
        allSpells: List<Spell>? = null,
        favoriteSpellIds: Set<String>? = null,
    ): SearchAndGroupSpellsResult {
        val normalizedQuery = query.trim()
        val resolvedSpells = allSpells ?: spellsRepository.allSpellsState.map {
            when (it) {
                is AssetState.Loading -> emptyList()
                is AssetState.Ready -> it.data
                is AssetState.Error -> emptyList()
            }
        }.first()
        val favoriteIds = if (favoriteOnly) {
            favoriteSpellIds ?: favoritesRepository.observeFavoriteIds(FavoriteType.SPELL).first().toSet()
        } else {
            emptySet()
        }
        val filteredSpells = resolvedSpells.filter { spell ->
            spell.matches(normalizedQuery, classes, favoriteOnly, favoriteIds)
        }

        return SearchAndGroupSpellsResult(
            spells = filteredSpells,
            spellsByLevel = filteredSpells.groupBy(Spell::level).toSortedMap(),
            totalCount = filteredSpells.size,
            query = normalizedQuery,
            classes = classes,
            favoriteOnly = favoriteOnly,
        )
    }

    private fun Spell.matches(
        query: String,
        classes: Set<EntityRef>,
        favoriteOnly: Boolean,
        favoriteIds: Set<String>,
    ): Boolean {
        val matchesQuery = query.isBlank() || name.contains(query, ignoreCase = true)
        val matchesClasses = classes.isEmpty() || classes.all { this.classes.contains(it) }
        val matchesFavorite = !favoriteOnly || id in favoriteIds
        return matchesQuery && matchesClasses && matchesFavorite
    }
}
