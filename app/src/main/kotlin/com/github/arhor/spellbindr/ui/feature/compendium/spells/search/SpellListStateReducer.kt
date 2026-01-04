package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.EntityRef

object SpellListStateReducer {

    sealed interface FilterEvent {
        data class QueryChanged(val query: String) : FilterEvent
        data object FavoritesToggled : FilterEvent
        data object FiltersOpened : FilterEvent
        data class FiltersSubmitted(val classes: Set<EntityRef>) : FilterEvent
        data class FiltersCanceled(val classes: Set<EntityRef>) : FilterEvent
    }

    @Immutable
    data class SpellFilters(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val currentClasses: Set<EntityRef> = emptySet(),
    )

    @Immutable
    data class SpellExpansionState(
        val expandedAll: Boolean = true,
        val expandedLevels: Map<Int, Boolean> = emptyMap(),
    )

    fun reduceFilters(filters: SpellFilters, event: FilterEvent): SpellFilters =
        when (event) {
            is FilterEvent.QueryChanged -> {
                val nextQuery = event.query.trim()
                if (nextQuery.equals(filters.query, ignoreCase = true)) {
                    filters
                } else {
                    filters.copy(query = nextQuery)
                }
            }

            FilterEvent.FavoritesToggled -> filters.copy(showFavorite = !filters.showFavorite)
            FilterEvent.FiltersOpened -> filters.copy(showFilterDialog = true)
            is FilterEvent.FiltersSubmitted -> filters.copy(
                showFilterDialog = false,
                currentClasses = if (event.classes == filters.currentClasses) {
                    filters.currentClasses
                } else {
                    event.classes
                },
            )

            is FilterEvent.FiltersCanceled -> filters.copy(
                showFilterDialog = false,
                currentClasses = if (event.classes == filters.currentClasses) {
                    filters.currentClasses
                } else {
                    event.classes
                },
            )
        }

    fun toggleGroup(
        state: SpellExpansionState,
        level: Int,
        currentExpandedLevels: Map<Int, Boolean>,
    ): SpellExpansionState {
        val currentExpanded = currentExpandedLevels[level] ?: state.expandedAll
        return state.copy(expandedLevels = state.expandedLevels + (level to !currentExpanded))
    }

    fun toggleAll(
        state: SpellExpansionState,
        levels: Set<Int>,
    ): SpellExpansionState {
        val nextExpandedAll = !state.expandedAll
        return state.copy(
            expandedAll = nextExpandedAll,
            expandedLevels = levels.associateWith { nextExpandedAll },
        )
    }

    fun expandedLevels(
        levels: Set<Int>,
        state: SpellExpansionState,
    ): Map<Int, Boolean> = levels.associateWith { level ->
        state.expandedLevels[level] ?: state.expandedAll
    }
}
