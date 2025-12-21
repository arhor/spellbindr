package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
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
    data class CharacterSpellPickerUiData(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sourceClass: String = "",
        val defaultSourceClass: String = "",
        val spellsState: SpellsState = SpellsState(),
    )

    sealed interface CharacterSpellPickerUiState {
        val sourceClass: String
        val defaultSourceClass: String
        val spellsState: SpellsState

        data class Loading(
            override val sourceClass: String,
            override val defaultSourceClass: String,
            override val spellsState: SpellsState,
        ) : CharacterSpellPickerUiState

        data class Content(
            override val sourceClass: String,
            override val defaultSourceClass: String,
            override val spellsState: SpellsState,
        ) : CharacterSpellPickerUiState

        data class Error(
            val message: String,
            override val sourceClass: String,
            override val defaultSourceClass: String,
            override val spellsState: SpellsState,
        ) : CharacterSpellPickerUiState
    }

    sealed interface CharacterSpellPickerUiAction {
        data class SourceClassChanged(val value: String) : CharacterSpellPickerUiAction
        data class QueryChanged(val query: String) : CharacterSpellPickerUiAction
        data object FiltersClicked : CharacterSpellPickerUiAction
        data object FavoritesClicked : CharacterSpellPickerUiAction
        data class SpellGroupToggled(val level: Int) : CharacterSpellPickerUiAction
        data object ToggleAllSpellGroups : CharacterSpellPickerUiAction
        data class FilterChanged(val classes: Set<EntityRef>) : CharacterSpellPickerUiAction
        data class SpellSelected(val spellId: String) : CharacterSpellPickerUiAction
    }

    sealed interface CharacterSpellPickerUiEvent {
        data class SourceClassChanged(val value: String) : CharacterSpellPickerUiEvent
        data class CharacterLoaded(val defaultSourceClass: String) : CharacterSpellPickerUiEvent
        data class ErrorChanged(val message: String?) : CharacterSpellPickerUiEvent
        data class SpellsStateUpdated(val spellsState: SpellsState) : CharacterSpellPickerUiEvent
    }

    sealed interface CharacterSpellPickerEffect {
        data class SpellAssignmentReady(val assignment: CharacterSpellAssignment) : CharacterSpellPickerEffect
    }

    private val characterId: String? = savedStateHandle.get<String>("characterId")
    private val _data = MutableStateFlow(
        CharacterSpellPickerUiData(
            isLoading = characterId != null,
            errorMessage = if (characterId == null) "Missing character id" else null,
        )
    )
    private val _effects = MutableSharedFlow<CharacterSpellPickerEffect>()
    val effects = _effects.asSharedFlow()
    val uiState: StateFlow<CharacterSpellPickerUiState> = _data
        .map { data -> data.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            _data.value.toUiState(),
        )
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
                }
        }
    }

    fun onAction(action: CharacterSpellPickerUiAction) {
        when (action) {
            is CharacterSpellPickerUiAction.SourceClassChanged ->
                updateData(CharacterSpellPickerUiEvent.SourceClassChanged(action.value))

            is CharacterSpellPickerUiAction.QueryChanged -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.QueryChanged(action.query),
                )
            }

            CharacterSpellPickerUiAction.FiltersClicked -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FiltersOpened)
            }

            CharacterSpellPickerUiAction.FavoritesClicked -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FavoritesToggled)
            }

            is CharacterSpellPickerUiAction.SpellGroupToggled -> spellExpansionState.update { state ->
                SpellListStateReducer.toggleGroup(
                    state = state,
                    level = action.level,
                    currentExpandedLevels = _data.value.spellsState.expandedSpellLevels,
                )
            }

            CharacterSpellPickerUiAction.ToggleAllSpellGroups -> spellExpansionState.update { state ->
                SpellListStateReducer.toggleAll(
                    state = state,
                    levels = _data.value.spellsState.spellsByLevel.keys,
                )
            }

            is CharacterSpellPickerUiAction.FilterChanged -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.FiltersSubmitted(action.classes),
                )
            }

            is CharacterSpellPickerUiAction.SpellSelected -> handleSpellSelected(action.spellId)
        }
    }

    private fun updateData(event: CharacterSpellPickerUiEvent) {
        _data.update { current -> reduce(current, event) }
    }

    private fun reduce(
        data: CharacterSpellPickerUiData,
        event: CharacterSpellPickerUiEvent,
    ): CharacterSpellPickerUiData = when (event) {
        is CharacterSpellPickerUiEvent.SourceClassChanged -> data.copy(sourceClass = event.value)
        is CharacterSpellPickerUiEvent.CharacterLoaded -> data.copy(
            isLoading = false,
            errorMessage = null,
            defaultSourceClass = event.defaultSourceClass,
            sourceClass = if (data.sourceClass.isBlank()) event.defaultSourceClass else data.sourceClass,
        )

        is CharacterSpellPickerUiEvent.ErrorChanged -> data.copy(
            isLoading = false,
            errorMessage = event.message,
        )

        is CharacterSpellPickerUiEvent.SpellsStateUpdated -> data.copy(spellsState = event.spellsState)
    }

    private fun CharacterSpellPickerUiData.toUiState(): CharacterSpellPickerUiState = when {
        isLoading -> CharacterSpellPickerUiState.Loading(
            sourceClass = sourceClass,
            defaultSourceClass = defaultSourceClass,
            spellsState = spellsState,
        )

        errorMessage != null -> CharacterSpellPickerUiState.Error(
            message = errorMessage,
            sourceClass = sourceClass,
            defaultSourceClass = defaultSourceClass,
            spellsState = spellsState,
        )

        else -> CharacterSpellPickerUiState.Content(
            sourceClass = sourceClass,
            defaultSourceClass = defaultSourceClass,
            spellsState = spellsState,
        )
    }

    private fun handleSpellSelected(spellId: String) {
        if (spellId.isBlank()) return
        val state = _data.value
        if (state.errorMessage != null) return
        val resolvedSource = state.sourceClass.takeIf { it.isNotBlank() } ?: state.defaultSourceClass
        viewModelScope.launch {
            _effects.emit(
                CharacterSpellPickerEffect.SpellAssignmentReady(
                    CharacterSpellAssignment(
                        spellId = spellId,
                        sourceClass = resolvedSource,
                    )
                )
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        spellsState
            .onEach { spellsState -> updateData(CharacterSpellPickerUiEvent.SpellsStateUpdated(spellsState)) }
            .launchIn(viewModelScope)

        characterId?.let { id ->
            characterRepository.observeCharacterSheet(id)
                .onEach { sheet ->
                    if (sheet != null) {
                        updateData(CharacterSpellPickerUiEvent.CharacterLoaded(sheet.className.trim()))
                    } else {
                        updateData(CharacterSpellPickerUiEvent.ErrorChanged("Character not found"))
                    }
                }
                .catch { throwable ->
                    updateData(CharacterSpellPickerUiEvent.ErrorChanged(
                        throwable.message ?: "Unable to load character",
                    ))
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
