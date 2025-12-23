package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Condition
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.GetSpellcastingClassRefsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAlignmentsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SearchAndGroupSpellsUseCase
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@Stable
@HiltViewModel
class CompendiumViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getSpellcastingClassRefsUseCase: GetSpellcastingClassRefsUseCase,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val observeAlignmentsUseCase: ObserveAlignmentsUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
    private val observeRacesUseCase: ObserveRacesUseCase,
    private val observeTraitsUseCase: ObserveTraitsUseCase,
    private val searchAndGroupSpellsUseCase: SearchAndGroupSpellsUseCase,
) : ViewModel() {

    sealed interface SpellsUiState {
        data object Loading : SpellsUiState

        @Immutable
        data class Loaded(
            val spells: List<Spell>,
            val spellsByLevel: Map<Int, List<Spell>>,
        ) : SpellsUiState

        @Immutable
        data class Error(val message: String) : SpellsUiState
    }

    @Immutable
    data class AlignmentsState(
        val alignments: List<Alignment> = emptyList(),
        val expandedItemName: String? = null,
    )

    @Immutable
    data class ConditionsState(
        val expandedItem: Condition? = null,
    )

    @Immutable
    data class RacesState(
        val races: List<Race> = emptyList(),
        val traits: Map<String, Trait> = emptyMap(),
        val expandedItemName: String? = null,
    )

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

    @Immutable
    data class CompendiumUiData(
        val alignmentsState: AlignmentsState = AlignmentsState(),
        val conditionsState: ConditionsState = ConditionsState(),
        val racesState: RacesState = RacesState(),
        val selectedSection: CompendiumSection = CompendiumSection.Spells,
        val spellsState: SpellsState = SpellsState(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
    )

    sealed interface CompendiumUiState {
        data object Loading : CompendiumUiState

        @Immutable
        data class Content(
            val alignmentsState: AlignmentsState,
            val conditionsState: ConditionsState,
            val racesState: RacesState,
            val selectedSection: CompendiumSection,
            val spellsState: SpellsState,
        ) : CompendiumUiState

        @Immutable
        data class Error(
            val message: String,
            val content: Content? = null,
        ) : CompendiumUiState
    }

    sealed interface CompendiumAction {
        data class SectionSelected(val section: CompendiumSection) : CompendiumAction
        data class SpellQueryChanged(val query: String) : CompendiumAction
        data object SpellFiltersClicked : CompendiumAction
        data object SpellFavoritesToggled : CompendiumAction
        data class SpellGroupToggled(val level: Int) : CompendiumAction
        data object SpellToggleAllGroups : CompendiumAction
        data class SpellFiltersSubmitted(val classes: Set<EntityRef>) : CompendiumAction
        data class SpellFiltersCanceled(val classes: Set<EntityRef>) : CompendiumAction
        data class AlignmentClicked(val alignmentName: String) : CompendiumAction
        data class ConditionClicked(val condition: Condition) : CompendiumAction
        data class RaceClicked(val raceName: String) : CompendiumAction
    }

    sealed interface CompendiumUiEvent {
        data class ContentUpdated(val content: CompendiumUiData) : CompendiumUiEvent
        data class ErrorChanged(val message: String?) : CompendiumUiEvent
    }

    private val alignmentSelection = MutableStateFlow<String?>(null)
    private val conditionSelection = MutableStateFlow<Condition?>(null)
    private val raceSelection = MutableStateFlow<String?>(null)
    private val spellFilters = MutableStateFlow(SpellListStateReducer.SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellListStateReducer.SpellExpansionState())
    private val logger = Logger.createLogger<CompendiumViewModel>()
    private val selectedSection =
        savedStateHandle.getStateFlow(SELECTED_SECTION_KEY, CompendiumSection.Spells)
    private val _data = MutableStateFlow(CompendiumUiData())

    val uiState: StateFlow<CompendiumUiState> = _data
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CompendiumUiState.Loading,
        )

    private val alignmentsState = combine(
        observeAlignmentsUseCase(),
        alignmentSelection,
    ) { alignments, expandedItemName ->
        AlignmentsState(
            alignments = alignments,
            expandedItemName = expandedItemName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlignmentsState())

    private val conditionsState = conditionSelection
        .map { ConditionsState(expandedItem = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConditionsState())

    private val racesState = combine(
        observeRacesUseCase(),
        observeTraitsUseCase(),
        raceSelection,
    ) { races, traits, expandedItemName ->
        RacesState(
            races = races,
            traits = traits.associateBy(Trait::id),
            expandedItemName = expandedItemName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RacesState())

    private val castingClassesState = flow {
        emit(getSpellcastingClassRefsUseCase())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
            emit(SpellsUiState.Loading)
            runCatching {
                searchAndGroupSpellsUseCase(
                    query = data.query,
                    classes = data.currentClasses,
                    favoriteOnly = data.showFavorite,
                    allSpells = data.allSpells,
                    favoriteSpellIds = data.favoriteSpellIdsSet,
                )
            }.onSuccess { result ->
                emit(SpellsUiState.Loaded(result.spells, result.spellsByLevel))
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to load spells." }
                emit(SpellsUiState.Error("Oops, something went wrong..."))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    private val spellsState = combine(
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

    fun onAction(action: CompendiumAction) {
        when (action) {
            is CompendiumAction.SectionSelected -> savedStateHandle[SELECTED_SECTION_KEY] = action.section
            is CompendiumAction.SpellQueryChanged -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.QueryChanged(action.query),
                )
            }

            CompendiumAction.SpellFiltersClicked -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FiltersOpened)
            }

            CompendiumAction.SpellFavoritesToggled -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(filters, SpellListStateReducer.FilterEvent.FavoritesToggled)
            }

            is CompendiumAction.SpellFiltersSubmitted -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.FiltersSubmitted(action.classes),
                )
            }

            is CompendiumAction.SpellFiltersCanceled -> spellFilters.update { filters ->
                SpellListStateReducer.reduceFilters(
                    filters,
                    SpellListStateReducer.FilterEvent.FiltersCanceled(action.classes),
                )
            }

            is CompendiumAction.SpellGroupToggled -> spellExpansionState.update { state ->
                SpellListStateReducer.toggleGroup(
                    state = state,
                    level = action.level,
                    currentExpandedLevels = spellsState.value.expandedSpellLevels,
                )
            }

            CompendiumAction.SpellToggleAllGroups -> spellExpansionState.update { state ->
                val levels = spellsState.value.spellsByLevel.keys
                SpellListStateReducer.toggleAll(
                    state = state,
                    levels = levels,
                )
            }

            is CompendiumAction.AlignmentClicked -> alignmentSelection.update { current ->
                if (current == action.alignmentName) {
                    null
                } else {
                    action.alignmentName
                }
            }

            is CompendiumAction.ConditionClicked -> conditionSelection.update { current ->
                if (current == action.condition) {
                    null
                } else {
                    action.condition
                }
            }

            is CompendiumAction.RaceClicked -> raceSelection.update { current ->
                if (current == action.raceName) {
                    null
                } else {
                    action.raceName
                }
            }
        }
    }

    private val contentState = combine(
        alignmentsState,
        conditionsState,
        racesState,
        selectedSection,
        spellsState,
    ) { alignments, conditions, races, section, spells ->
        CompendiumUiData(
            alignmentsState = alignments,
            conditionsState = conditions,
            racesState = races,
            selectedSection = section,
            spellsState = spells,
            isLoading = false,
            errorMessage = null,
        )
    }

    init {
        contentState
            .onEach { updateData(CompendiumUiEvent.ContentUpdated(it)) }
            .launchIn(viewModelScope)
    }

    private fun updateData(event: CompendiumUiEvent) {
        _data.update { current -> reduce(current, event) }
    }

    private fun reduce(data: CompendiumUiData, event: CompendiumUiEvent): CompendiumUiData = when (event) {
        is CompendiumUiEvent.ContentUpdated -> data.copy(
            alignmentsState = event.content.alignmentsState,
            conditionsState = event.content.conditionsState,
            racesState = event.content.racesState,
            selectedSection = event.content.selectedSection,
            spellsState = event.content.spellsState,
            isLoading = event.content.isLoading,
            errorMessage = event.content.errorMessage,
        )

        is CompendiumUiEvent.ErrorChanged -> data.copy(
            errorMessage = event.message,
            isLoading = false,
        )
    }

    private fun CompendiumUiData.toUiState(): CompendiumUiState = when {
        isLoading -> CompendiumUiState.Loading
        errorMessage != null -> CompendiumUiState.Error(
            message = errorMessage,
            content = CompendiumUiState.Content(
                alignmentsState = alignmentsState,
                conditionsState = conditionsState,
                racesState = racesState,
                selectedSection = selectedSection,
                spellsState = spellsState,
            ),
        )

        else -> CompendiumUiState.Content(
            alignmentsState = alignmentsState,
            conditionsState = conditionsState,
            racesState = racesState,
            selectedSection = selectedSection,
            spellsState = spellsState,
        )
    }

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

    private companion object {
        const val SELECTED_SECTION_KEY = "compendium_section"
    }
}
