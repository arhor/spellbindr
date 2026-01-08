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
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import com.github.arhor.spellbindr.utils.mapWhenReady
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
        val expandedLevels: Map<Int, Boolean> = emptyMap(),
    )

    private data class SpellsQuery(
        val filters: SpellFilters,
        val allSpellsState: Loadable<List<Spell>>,
        val favoriteSpellIds: List<String>,
    ) {
        val favoriteSpellIdsSet: Set<String> = favoriteSpellIds.toSet()
    }

    private val spellFilters = MutableStateFlow(SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellExpansionState())

    private val spellsUiState = combine(
        spellFilters,
        observeAllSpells(),
        observeFavoriteSpellIds(),
        ::SpellsQuery,
    )
        .debounce(350.milliseconds)
        .distinctUntilChanged()
        .transformLatest { data ->
            when (val state = data.allSpellsState) {
                is Loadable.Loading -> emit(SpellsUiState.Loading)
                is Loadable.Error -> {
                    logger.error(state.cause) { "Failed to load spells." }
                    emit(SpellsUiState.Error("Failed to load spells."))
                }

                is Loadable.Ready -> {
                    emit(SpellsUiState.Loading)
                    runCatching {
                        searchAndGroupSpells(
                            query = data.filters.query,
                            classes = data.filters.currentClasses,
                            favoriteOnly = data.filters.showFavorite,
                            allSpells = state.data,
                            favoriteSpellIds = data.favoriteSpellIdsSet,
                        )
                    }.onSuccess { result ->
                        emit(SpellsUiState.Content(result.spells, result.spellsByLevel))
                    }.onFailure { throwable ->
                        logger.error(throwable) { "Failed to load spells." }
                        emit(SpellsUiState.Error("Oops, something went wrong..."))
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    val uiState: StateFlow<State> = combine(
        spellsUiState,
        spellFilters,
        spellExpansionState,
        observeSpellcastingClasses().mapWhenReady { classes -> classes.map { EntityRef(it.id) } },
    ) { spellsUiState, filters, expansionState, castingClasses ->
        when (spellsUiState) {
            is SpellsUiState.Loading -> State(
                query = filters.query,
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                currentClasses = filters.currentClasses,
                uiState = SpellsUiState.Loading,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Error -> State(
                query = filters.query,
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                currentClasses = filters.currentClasses,
                uiState = spellsUiState,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Content -> {
                val expandedLevels = expandedLevels(
                    levels = spellsUiState.spellsByLevel.keys,
                    state = expansionState,
                )
                State(
                    query = filters.query,
                    showFavorite = filters.showFavorite,
                    showFilterDialog = filters.showFilterDialog,
                    castingClasses = (castingClasses as? Loadable.Ready)?.data ?: emptyList(),
                    currentClasses = filters.currentClasses,
                    uiState = spellsUiState,
                    spellsByLevel = spellsUiState.spellsByLevel,
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
            state.copy(expandedLevels = state.expandedLevels + (level to !isExpanded))
        }
    }

    fun onToggleAllGroups() {
        spellExpansionState.update { state ->
            val levels = uiState.value.spellsByLevel.keys
            val nextExpandedAll = !state.expandedAll
            state.copy(
                expandedAll = nextExpandedAll,
                expandedLevels = levels.associateWith { nextExpandedAll },
            )
        }
    }

    private fun expandedLevels(
        levels: Set<Int>,
        state: SpellExpansionState,
    ): Map<Int, Boolean> = levels.associateWith { level ->
        state.expandedLevels[level] ?: state.expandedAll
    }

    companion object {
        private val logger = createLogger()
    }
}
