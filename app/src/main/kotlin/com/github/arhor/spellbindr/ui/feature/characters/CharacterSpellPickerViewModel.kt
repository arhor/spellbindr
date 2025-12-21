package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.SpellListState
import com.github.arhor.spellbindr.ui.feature.compendium.SpellListStateReducer
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class CharacterSpellPickerViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Immutable
    data class SpellsState(
        override val query: String = "",
        override val showFavorite: Boolean = false,
        override val showFilterDialog: Boolean = false,
        override val castingClasses: List<EntityRef> = emptyList(),
        override val currentClasses: Set<EntityRef> = emptySet(),
        override val uiState: CompendiumViewModel.SpellsUiState = CompendiumViewModel.SpellsUiState.Loading,
        override val spellsByLevel: Map<Int, List<Spell>> = emptyMap(),
        override val expandedSpellLevels: Map<Int, Boolean> = emptyMap(),
        override val expandedAll: Boolean = true,
    ) : SpellListState

    @Immutable
    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sourceClass: String = "",
        val defaultSourceClass: String = "",
        val spellsState: SpellsState = SpellsState(),
    )

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _state = MutableStateFlow(
        State(
            isLoading = characterId != null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        )
    )
    val state: StateFlow<State> = _state
    private val castingClassesState = MutableStateFlow<List<EntityRef>>(emptyList())
    private val spellFilters = MutableStateFlow(SpellListStateReducer.SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellListStateReducer.SpellExpansionState())
    private val logger = Logger.createLogger<CharacterSpellPickerViewModel>()

    init {
        observeStateChanges()
        viewModelScope.launch {
            characterClassRepository
                .findSpellcastingClassesRefs()
                .let { refs ->
                    castingClassesState.value = refs
                    _state.update {
                        it.copy(
                            spellsState = it.spellsState.copy(
                                castingClasses = refs,
                            )
                        )
                    }
                }
        }
    }

    fun onSourceClassChanged(value: String) {
        _state.update { it.copy(sourceClass = value) }
    }

    fun buildAssignment(spellId: String): CharacterSpellAssignment? {
        val state = _state.value
        if (spellId.isBlank() || state.errorMessage != null) return null
        val resolvedSource = state.sourceClass.takeIf { it.isNotBlank() } ?: state.defaultSourceClass
        return CharacterSpellAssignment(
            spellId = spellId,
            sourceClass = resolvedSource,
        )
    }

    fun onFavoritesClicked() {
        spellFilters.update { filters ->
            SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FavoritesToggled)
        }
    }

    fun onSpellGroupToggled(level: Int) {
        spellExpansionState.update { state ->
            SpellListStateReducer.toggleGroup(
                state = state,
                level = level,
                currentExpandedLevels = _state.value.spellsState.expandedSpellLevels,
            )
        }
    }

    fun onToggleAllSpellGroups() {
        spellExpansionState.update { state ->
            SpellListStateReducer.toggleAll(
                state = state,
                levels = _state.value.spellsState.spellsByLevel.keys,
            )
        }
    }

    fun onFilterClicked() {
        spellFilters.update { filters ->
            SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FiltersOpened)
        }
    }

    fun onQueryChanged(query: String) {
        spellFilters.update { filters ->
            SpellListStateReducer.reduceFilters(
                filters,
                SpellListStateReducer.FilterEvent.QueryChanged(query),
            )
        }
    }

    fun onFilterChanged(classes: Set<EntityRef>) {
        spellFilters.update { filters ->
            SpellListStateReducer.reduceFilters(
                filters,
                SpellListStateReducer.FilterEvent.FiltersSubmitted(classes),
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        spellsState
            .onEach { spellsState -> _state.update { it.copy(spellsState = spellsState) } }
            .launchIn(viewModelScope)

        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    if (sheet != null) {
                        _state.update { state ->
                            val fallback = sheet.className.trim()
                            state.copy(
                                isLoading = false,
                                defaultSourceClass = fallback,
                                sourceClass = if (state.sourceClass.isBlank()) fallback else state.sourceClass,
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Character not found",
                            )
                        }
                    }
                }
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load character",
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    @OptIn(FlowPreview::class)
    private val spellsUiState = combine(
        spellFilters,
        observeAllSpellsUseCase(),
        observeFavoriteSpellIdsUseCase(),
        ::SpellsQuery,
    )
        .debounce(350.milliseconds)
        .distinctUntilChanged()
        .transformLatest { data ->
            emit(CompendiumViewModel.SpellsUiState.Loading)
            runCatching {
                searchAndGroupSpellsUseCase(
                    query = data.query,
                    classes = data.currentClasses,
                    favoriteOnly = data.showFavorite,
                    allSpells = data.allSpells,
                    favoriteSpellIds = data.favoriteSpellIdsSet,
                )
            }.onSuccess { result ->
                emit(
                    CompendiumViewModel.SpellsUiState.Loaded(
                        spells = result.spells,
                        spellsByLevel = result.spellsByLevel,
                    )
                )
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to load spells." }
                emit(CompendiumViewModel.SpellsUiState.Error("Oops, something went wrong..."))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompendiumViewModel.SpellsUiState.Loading)

    private val spellsState = combine(
        spellFilters,
        spellExpansionState,
        castingClassesState,
        spellsUiState,
    ) { filters, expansionState, castingClasses, uiState ->
        val spellsByLevel = when (uiState) {
            is CompendiumViewModel.SpellsUiState.Loaded -> uiState.spellsByLevel
            else -> emptyMap()
        }
        val expandedSpellLevels = when (uiState) {
            is CompendiumViewModel.SpellsUiState.Loaded -> SpellListStateReducer.expandedLevels(
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

    private data class SpellsQuery(
        val filters: SpellListStateReducer.SpellFilters,
        val allSpells: List<Spell>,
        val favoriteSpellIds: List<String>,
    ) {
        val query: String = filters.query
        val currentClasses: Set<EntityRef> = filters.currentClasses
        val showFavorite: Boolean = filters.showFavorite
        val favoriteSpellIdsSet: Set<String> = favoriteSpellIds.toSet()
    }

}
