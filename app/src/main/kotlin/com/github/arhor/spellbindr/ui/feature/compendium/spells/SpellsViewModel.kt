package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsStateUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.utils.mapWhenReady
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class SpellsViewModel @Inject constructor(
    private val observeSpellcastingClasses: ObserveSpellcastingClassesUseCase,
    private val observeAllSpells: ObserveAllSpellsStateUseCase,
    private val observeFavoriteSpellIds: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpells: SearchAndGroupSpellsUseCase,
) : ViewModel() {

    data class State(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val castingClasses: List<EntityRef> = emptyList(),
        val currentClasses: List<EntityRef> = emptyList(),
        val uiState: SpellsUiState = SpellsUiState.Loading,
        val spellsByLevel: Map<Int, List<Spell>> = emptyMap(),
        val expandedSpellLevels: Map<Int, Boolean> = emptyMap(),
        val expandedAll: Boolean = true,
    )

    private data class SpellFilters(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val currentClasses: List<EntityRef> = emptyList(),
    )

    private data class SpellExpansionState(
        val expandedAll: Boolean = true,
        val expandedSpellLevels: Map<Int, Boolean> = emptyMap(),
    )

    private val spellFilters = MutableStateFlow(SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellExpansionState())

    private val _state: StateFlow<SpellsUiState> = combine(
        spellFilters,
        observeAllSpells(),
        observeFavoriteSpellIds(),
    ) { filters, allSpells, favoriteSpellIds ->
        when (allSpells) {
            is Loadable.Loading -> {
                SpellsUiState.Loading
            }

            is Loadable.Ready -> {
                searchAndGroupSpells(
                    query = filters.query,
                    classes = filters.currentClasses,
                    favoriteOnly = filters.showFavorite,
                    allSpells = allSpells.data,
                    favoriteSpellIds = favoriteSpellIds.toSet(),
                ).let {
                    SpellsUiState.Content(
                        query = filters.query,
                        spells = it.spells,
                        spellsByLevel = it.spellsByLevel
                    )
                }
            }

            is Loadable.Error -> {
                SpellsUiState.Error("Failed to load spells.")
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    val uiState: StateFlow<State> = combine(
        _state,
        spellFilters,
        spellExpansionState,
        observeSpellcastingClasses().mapWhenReady { classes -> classes.map { EntityRef(it.id) } },
    ) { state, filters, expansionState, castingClasses ->
        when (state) {
            is SpellsUiState.Loading -> State(
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                currentClasses = filters.currentClasses,
                uiState = SpellsUiState.Loading,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Error -> State(
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                currentClasses = filters.currentClasses,
                uiState = state,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Content -> {
                val expandedLevels = expandedLevels(
                    levels = state.spellsByLevel.keys,
                    state = expansionState,
                )
                State(
                    query = state.query,
                    showFavorite = filters.showFavorite,
                    showFilterDialog = filters.showFilterDialog,
                    castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                    currentClasses = filters.currentClasses,
                    uiState = state,
                    spellsByLevel = state.spellsByLevel,
                    expandedSpellLevels = expandedLevels,
                    expandedAll = expansionState.expandedAll,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    fun onQueryChanged(query: String) {
        val sanitizedQuery = query.trim()
        spellFilters.update { it.copy(query = sanitizedQuery) }
    }

    fun onFiltersClick() {
        spellFilters.update { it.copy(showFilterDialog = true) }
    }

    fun onFavoritesToggled() {
        spellFilters.update { it.copy(showFavorite = !it.showFavorite) }
    }

    fun onFiltersSubmitted(classes: List<EntityRef>) {
        spellFilters.update {
            it.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }

    fun onFiltersCanceled(classes: List<EntityRef>) {
        spellFilters.update {
            it.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }

    fun onGroupToggled(level: Int) {
        spellExpansionState.update { state ->
            val isExpanded = uiState.value.expandedSpellLevels[level] ?: state.expandedAll
            state.copy(expandedSpellLevels = state.expandedSpellLevels + (level to !isExpanded))
        }
    }

    fun onToggleAllGroups() {
        spellExpansionState.update { state ->
            val levels = uiState.value.spellsByLevel.keys
            val nextExpandedAll = !state.expandedAll
            state.copy(
                expandedAll = nextExpandedAll,
                expandedSpellLevels = levels.associateWith { nextExpandedAll },
            )
        }
    }

    private fun expandedLevels(
        levels: Set<Int>,
        state: SpellExpansionState,
    ): Map<Int, Boolean> = levels.associateWith { level ->
        state.expandedSpellLevels[level] ?: state.expandedAll
    }
}
