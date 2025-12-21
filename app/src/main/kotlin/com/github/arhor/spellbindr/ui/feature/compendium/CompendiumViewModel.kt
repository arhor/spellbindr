package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.ReferenceDataRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveFavoriteSpellIdsUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@Stable
@HiltViewModel
class CompendiumViewModel @Inject constructor(
    private val characterClassRepository: CharacterClassRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val savedStateHandle: SavedStateHandle,
    private val observeAllSpellsUseCase: ObserveAllSpellsUseCase,
    private val observeFavoriteSpellIdsUseCase: ObserveFavoriteSpellIdsUseCase,
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

    sealed interface SpellsEvent {
        data class QueryChanged(val query: String) : SpellsEvent
        data object FavoritesToggled : SpellsEvent
        data object FiltersOpened : SpellsEvent
        data class FiltersSubmitted(val classes: Set<EntityRef>) : SpellsEvent
        data class FiltersCanceled(val classes: Set<EntityRef>) : SpellsEvent
        data class GroupToggled(val level: Int) : SpellsEvent
        data object ToggleAllGroups : SpellsEvent
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
    data class State(
        val alignmentsState: AlignmentsState = AlignmentsState(),
        val conditionsState: ConditionsState = ConditionsState(),
        val racesState: RacesState = RacesState(),
        val selectedSection: CompendiumSection = CompendiumSection.Spells,
        val spellsState: SpellsState = SpellsState(),
    )

    private val alignmentSelection = MutableStateFlow<String?>(null)
    private val conditionSelection = MutableStateFlow<Condition?>(null)
    private val raceSelection = MutableStateFlow<String?>(null)
    private val spellFilters = MutableStateFlow(SpellListStateReducer.SpellFilters())
    private val spellExpansionState = MutableStateFlow(SpellListStateReducer.SpellExpansionState())
    private val logger = Logger.createLogger<CompendiumViewModel>()
    private val selectedSection =
        savedStateHandle.getStateFlow(SELECTED_SECTION_KEY, CompendiumSection.Spells)

    private val alignmentsState = combine(
        referenceDataRepository.allAlignments,
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
        referenceDataRepository.allRaces,
        referenceDataRepository.allTraits,
        raceSelection,
    ) { races, traits, expandedItemName ->
        RacesState(
            races = races,
            traits = traits.associateBy(Trait::id),
            expandedItemName = expandedItemName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RacesState())

    private val castingClassesState = flow {
        emit(characterClassRepository.findSpellcastingClassesRefs())
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

    val state: StateFlow<State> = combine(
        alignmentsState,
        conditionsState,
        racesState,
        selectedSection,
        spellsState,
    ) { alignments, conditions, races, section, spells ->
        State(
            alignmentsState = alignments,
            conditionsState = conditions,
            racesState = races,
            selectedSection = section,
            spellsState = spells,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    fun onSpellEvent(event: SpellsEvent) {
        when (event) {
            is SpellsEvent.GroupToggled -> {
                spellExpansionState.update { state ->
                    SpellListStateReducer.toggleGroup(
                        state = state,
                        level = event.level,
                        currentExpandedLevels = spellsState.value.expandedSpellLevels,
                    )
                }
            }

            SpellsEvent.ToggleAllGroups -> {
                spellExpansionState.update { state ->
                    val levels = spellsState.value.spellsByLevel.keys
                    SpellListStateReducer.toggleAll(
                        state = state,
                        levels = levels,
                    )
                }
            }

            else -> spellFilters.update { filters ->
                event.toFilterEvent()?.let { SpellListStateReducer.reduceFilters(filters, it) } ?: filters
            }
        }
    }

    fun handleAlignmentClick(alignmentName: String) {
        alignmentSelection.update { current ->
            if (current == alignmentName) {
                null
            } else {
                alignmentName
            }
        }
    }

    fun handleConditionClick(condition: Condition) {
        conditionSelection.update { current ->
            if (current == condition) {
                null
            } else {
                condition
            }
        }
    }

    fun handleRaceClick(raceName: String) {
        raceSelection.update { current ->
            if (current == raceName) {
                null
            } else {
                raceName
            }
        }
    }

    fun onSectionSelected(section: CompendiumSection) {
        savedStateHandle[SELECTED_SECTION_KEY] = section
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

    private fun SpellsEvent.toFilterEvent(): SpellListStateReducer.FilterEvent? =
        when (this) {
            is SpellsEvent.QueryChanged -> SpellListStateReducer.FilterEvent.QueryChanged(query)
            SpellsEvent.FavoritesToggled -> SpellListStateReducer.FilterEvent.FavoritesToggled
            SpellsEvent.FiltersOpened -> SpellListStateReducer.FilterEvent.FiltersOpened
            is SpellsEvent.FiltersSubmitted -> SpellListStateReducer.FilterEvent.FiltersSubmitted(classes)
            is SpellsEvent.FiltersCanceled -> SpellListStateReducer.FilterEvent.FiltersCanceled(classes)
            is SpellsEvent.GroupToggled -> null
            SpellsEvent.ToggleAllGroups -> null
        }
}
