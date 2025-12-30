package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.GetSpellcastingClassRefsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsStateUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellListState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellListStateReducer
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Stable
@HiltViewModel
class SpellsViewModel @Inject constructor(
    private val getSpellcastingClassRefsUseCase: GetSpellcastingClassRefsUseCase,
    private val observeAllSpellsStateUseCase: ObserveAllSpellsStateUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
) : ViewModel() {

    sealed interface Action {
        data class QueryChanged(val query: String) : Action
        data object FiltersClicked : Action
        data object FavoritesToggled : Action
        data class GroupToggled(val level: Int) : Action
        data object ToggleAllGroups : Action
        data class FiltersSubmitted(val classes: Set<EntityRef>) : Action
        data class FiltersCanceled(val classes: Set<EntityRef>) : Action
        data class SpellClicked(val spell: Spell) : Action
    }

    sealed interface Effect {
        data class SpellSelected(val spell: Spell) : Effect
    }

    @Immutable
    data class SpellsState(
        override val query: String = "",
        override val showFavorite: Boolean = false,
        override val showFilterDialog: Boolean = false,
        override val castingClasses: List<EntityRef> = emptyList(),
        override val currentClasses: Set<EntityRef> = emptySet(),
        override val uiState: SpellsUiState = SpellsUiState.Loading,
        override val spellsByLevel: Map<Int, List<Spell>> = emptyMap(),
        override val expandedSpellLevels: Map<Int, Boolean> = emptyMap(),
        override val expandedAll: Boolean = true,
    ) : SpellListState

    private val _effects = MutableSharedFlow<Effect>()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    private val spellFilters = MutableStateFlow(SpellListStateReducer.SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellListStateReducer.SpellExpansionState())

    private val logger = Logger.createLogger<SpellsViewModel>()

    private val castingClassesState = flow {
        emit(getSpellcastingClassRefsUseCase())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val spellsUiState = combine(
        spellFilters,
        observeAllSpellsStateUseCase(),
        observeFavoriteSpellIdsUseCase(),
        ::SpellsQuery,
    )
        .debounce(350.milliseconds)
        .distinctUntilChanged()
        .transformLatest { data ->
            when (val state = data.allSpellsState) {
                is AssetState.Loading -> emit(SpellsUiState.Loading)
                is AssetState.Error -> {
                    logger.error(state.cause) { "Failed to load spells." }
                    emit(SpellsUiState.Error("Failed to load spells."))
                }

                is AssetState.Ready -> {
                    emit(SpellsUiState.Loading)
                    runCatching {
                        searchAndGroupSpellsUseCase(
                            query = data.query,
                            classes = data.currentClasses,
                            favoriteOnly = data.showFavorite,
                            allSpells = state.data,
                            favoriteSpellIds = data.favoriteSpellIdsSet,
                        )
                    }.onSuccess { result ->
                        emit(SpellsUiState.Loaded(result.spells, result.spellsByLevel))
                    }.onFailure { throwable ->
                        logger.error(throwable) { "Failed to load spells." }
                        emit(SpellsUiState.Error("Oops, something went wrong..."))
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    val spellsState = combine(
        spellFilters,
        spellExpansionState,
        castingClassesState,
        spellsUiState,
    ) { filters, expansionState, castingClasses, uiState ->
        val spellsByLevel = when (uiState) {
            is SpellsUiState.Loaded -> uiState.spellsByLevel
            else -> emptyMap()
        }
        val expandedSpellLevels = when (uiState) {
            is SpellsUiState.Loaded -> SpellListStateReducer.expandedLevels(
                levels = uiState.spellsByLevel.keys,
                state = expansionState,
            )

            else -> emptyMap()
        }
        SpellsState(
            query = filters.query,
            showFavorite = filters.showFavorite,
            showFilterDialog = filters.showFilterDialog,
            castingClasses = castingClasses,
            currentClasses = filters.currentClasses,
            uiState = uiState,
            spellsByLevel = spellsByLevel,
            expandedSpellLevels = expandedSpellLevels,
            expandedAll = expansionState.expandedAll,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsState())

    fun onAction(action: Action) {
        when (action) {
            is Action.QueryChanged -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.QueryChanged(action.query),
                )
            }

            Action.FiltersClicked -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FiltersOpened)
            }

            Action.FavoritesToggled -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FavoritesToggled)
            }

            is Action.FiltersSubmitted -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.FiltersSubmitted(action.classes),
                )
            }

            is Action.FiltersCanceled -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.FiltersCanceled(action.classes),
                )
            }

            is Action.SpellClicked -> viewModelScope.launch {
                _effects.emit(Effect.SpellSelected(action.spell))
            }

            is Action.GroupToggled -> spellExpansionState.update { state ->
                SpellListStateReducer.toggleGroup(
                    state = state,
                    level = action.level,
                    currentExpandedLevels = spellsState.value.expandedSpellLevels,
                )
            }

            Action.ToggleAllGroups -> spellExpansionState.update { state ->
                val levels = spellsState.value.spellsByLevel.keys
                SpellListStateReducer.toggleAll(
                    state = state,
                    levels = levels,
                )
            }
        }
    }

    private data class SpellsQuery(
        val filters: SpellListStateReducer.SpellFilters,
        val allSpellsState: AssetState<List<Spell>>,
        val favoriteSpellIds: List<String>,
    ) {
        val query: String = filters.query
        val currentClasses: Set<EntityRef> = filters.currentClasses
        val showFavorite: Boolean = filters.showFavorite
        val favoriteSpellIdsSet: Set<String> = favoriteSpellIds.toSet()
    }
}
