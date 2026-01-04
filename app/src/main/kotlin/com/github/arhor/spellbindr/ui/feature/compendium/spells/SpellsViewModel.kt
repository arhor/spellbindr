package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.usecase.GetSpellcastingClassRefsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsStateUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellListState
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Stable
@HiltViewModel
class SpellsViewModel @Inject constructor(
    private val getSpellcastingClassRefsUseCase: GetSpellcastingClassRefsUseCase,
    private val observeAllSpellsStateUseCase: ObserveAllSpellsStateUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
) : ViewModel() {

    data class State(
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

    private data class SpellFilters(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val currentClasses: Set<EntityRef> = emptySet(),
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

    private val _spellSelections = MutableSharedFlow<Spell>()
    val spellSelections: SharedFlow<Spell> = _spellSelections.asSharedFlow()

    private val spellFilters = MutableStateFlow(SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellExpansionState())

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
                is Loadable.Loading -> emit(SpellsUiState.Loading)
                is Loadable.Error -> {
                    logger.error(state.cause) { "Failed to load spells." }
                    emit(SpellsUiState.Error("Failed to load spells."))
                }

                is Loadable.Ready -> {
                    emit(SpellsUiState.Loading)
                    runCatching {
                        searchAndGroupSpellsUseCase(
                            query = data.filters.query,
                            classes = data.filters.currentClasses,
                            favoriteOnly = data.filters.showFavorite,
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

    val uiState = combine(
        spellsUiState,
        spellFilters,
        spellExpansionState,
        castingClassesState,
    ) { spellsUiState, filters, expansionState, castingClasses ->
        when (spellsUiState) {
            is SpellsUiState.Loading -> State(
                query = filters.query,
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = castingClasses,
                currentClasses = filters.currentClasses,
                uiState = SpellsUiState.Loading,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Error -> State(
                query = filters.query,
                showFavorite = filters.showFavorite,
                showFilterDialog = filters.showFilterDialog,
                castingClasses = castingClasses,
                currentClasses = filters.currentClasses,
                uiState = spellsUiState,
                expandedAll = expansionState.expandedAll,
            )

            is SpellsUiState.Loaded -> {
                val expandedLevels = expandedLevels(
                    levels = spellsUiState.spellsByLevel.keys,
                    state = expansionState,
                )
                State(
                    query = filters.query,
                    showFavorite = filters.showFavorite,
                    showFilterDialog = filters.showFilterDialog,
                    castingClasses = castingClasses,
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

    fun onFiltersSubmitted(classes: Set<EntityRef>) {
        spellFilters.update {
            it.copy(
                showFilterDialog = false,
                currentClasses = classes,
            )
        }
    }

    fun onFiltersCanceled(classes: Set<EntityRef>) {
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

    fun onSpellSelected(spell: Spell) {
        viewModelScope.launch {
            _spellSelections.emit(spell)
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
