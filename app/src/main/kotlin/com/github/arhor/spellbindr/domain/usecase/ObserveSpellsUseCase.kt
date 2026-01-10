package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.map
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case to observe a stream of spells, allowing for filtering by name, character classes,
 * and favorite status.
 *
 * This class combines spell data from the [SpellsRepository] with favorite status
 * from the [FavoritesRepository] to provide a reactive list of [Spell] objects
 * wrapped in a [Loadable] state.
 *
 * @property spellsRepository Repository providing the source of all available spells.
 * @property favoritesRepository Repository providing the stream of favorite spell IDs.
 */
@Singleton
class ObserveSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
    private val favoritesRepository: FavoritesRepository,
) {
    /**
     * Observes a stream of spells filtered by the provided criteria.
     *
     * @param query a string to filter spells by name (case-insensitive)
     * @param characterClasses a set of character classes to filter spells by
     * @param getFavoritesOnly if true, restricts the result to spells marked as favorites
     *
     * @return a flow containing a loadable list of spells matching the filters
     */
    operator fun invoke(
        query: String = "",
        characterClasses: Set<EntityRef> = emptySet(),
        getFavoritesOnly: Boolean = false,
    ): Flow<Loadable<List<Spell>>> {

        return combine(
            spellsRepository.allSpellsState,
            favoritesRepository.observeFavoriteIds(FavoriteType.SPELL),
        ) { spells, favoriteSpellIds ->
            spells.map { data ->
                val normalizedQuery = query.trim()
                val favoritesFilter = if (getFavoritesOnly) favoriteSpellIds else emptySet()

                data.filter { it.matches(normalizedQuery, characterClasses, favoritesFilter) }
            }
        }.catch { emit(Loadable.Failure(errorMessage = "Failed to load spells", cause = it)) }
    }

    private fun Spell.matches(
        queryFilter: String,
        classesFilter: Set<EntityRef>,
        favoritesFilter: Set<String>,
    ): Boolean {
        return id matchesFavoritesFilter favoritesFilter
            && name matchesQueryFilter queryFilter
            && classes matchesClassesFilter classesFilter
    }

    private infix fun String.matchesQueryFilter(query: String): Boolean =
        query.isEmpty() || this.contains(query, ignoreCase = true)

    private infix fun List<EntityRef>.matchesClassesFilter(characterClasses: Set<EntityRef>): Boolean =
        characterClasses.isEmpty() || characterClasses.any { it in this }

    private infix fun String.matchesFavoritesFilter(favoriteSpellIds: Set<String>): Boolean =
        favoriteSpellIds.isEmpty() || this in favoriteSpellIds
}
