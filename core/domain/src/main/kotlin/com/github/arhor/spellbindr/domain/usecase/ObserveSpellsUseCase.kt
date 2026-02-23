package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.mapContent
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.domain.usecase.internal.matchesFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
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
            spells to favoriteSpellIds
        }.mapLatest { (spells, favoriteSpellIds) ->
            spells.mapContent { data ->
                val favoritesFilter = if (getFavoritesOnly) favoriteSpellIds else emptySet()

                data.filter { it.matchesFilters(query, characterClasses, favoritesFilter) }
            }
        }.flowOn(Dispatchers.Default)
            .catch { emit(Loadable.Failure(errorMessage = "Failed to load spells", cause = it)) }
    }
}
