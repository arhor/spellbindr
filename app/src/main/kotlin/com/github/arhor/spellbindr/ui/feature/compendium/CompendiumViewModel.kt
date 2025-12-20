package com.github.arhor.spellbindr.ui.feature.compendium

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Alignment
import com.github.arhor.spellbindr.data.model.Trait
import com.github.arhor.spellbindr.data.model.next.CharacterRace
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.data.repository.AlignmentRepository
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.data.repository.RacesRepository
import com.github.arhor.spellbindr.data.repository.TraitsRepository
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
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

@Stable
@HiltViewModel
class CompendiumViewModel @Inject constructor(
    private val alignmentRepository: AlignmentRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val racesRepository: RacesRepository,
    private val savedStateHandle: SavedStateHandle,
    private val spellRepository: SpellsRepository,
    private val traitsRepository: TraitsRepository,
) : ViewModel() {

    sealed interface SpellsUiState {
        data object Loading : SpellsUiState

        @Immutable
        data class Loaded(val spells: List<Spell>) : SpellsUiState

        @Immutable
        data class Error(val message: String) : SpellsUiState
    }

    sealed interface SpellsEvent {
        data class QueryChanged(val query: String) : SpellsEvent
        data object FavoritesToggled : SpellsEvent
        data object FiltersOpened : SpellsEvent
        data class FiltersSubmitted(val classes: Set<EntityRef>) : SpellsEvent
        data class FiltersCanceled(val classes: Set<EntityRef>) : SpellsEvent
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
        val races: List<CharacterRace> = emptyList(),
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
    private val spellFilters = MutableStateFlow(SpellFilters())
    private val logger = Logger.createLogger<CompendiumViewModel>()
    private val selectedSection =
        savedStateHandle.getStateFlow(SELECTED_SECTION_KEY, CompendiumSection.Spells)

    private val alignmentsState = combine(
        alignmentRepository.allAlignments,
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
        racesRepository.allRaces,
        traitsRepository.allTraits,
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
        spellRepository.allSpells,
        spellRepository.favoriteSpellIds,
        ::SpellsQuery,
    )
        .debounce(350.milliseconds)
        .distinctUntilChanged()
        .transformLatest { data ->
            emit(SpellsUiState.Loading)
            runCatching {
                spellRepository.findSpells(
                    query = data.query,
                    classes = data.currentClasses,
                    favoriteOnly = data.showFavorite,
                )
            }.onSuccess { spells ->
                emit(SpellsUiState.Loaded(spells))
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to load spells." }
                emit(SpellsUiState.Error("Oops, something went wrong..."))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpellsUiState.Loading)

    private val spellsState = combine(
        spellFilters,
        castingClassesState,
        spellsUiState,
    ) { filters, castingClasses, uiState ->
        SpellsState(
            query = filters.query,
            showFavorite = filters.showFavorite,
            showFilterDialog = filters.showFilterDialog,
            castingClasses = castingClasses,
            currentClasses = filters.currentClasses,
            uiState = uiState,
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
        spellFilters.update { reduceSpellFilters(it, event) }
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

    private fun reduceSpellFilters(filters: SpellFilters, event: SpellsEvent): SpellFilters =
        when (event) {
            is SpellsEvent.QueryChanged -> {
                val nextQuery = event.query.trim()
                if (nextQuery.equals(filters.query, ignoreCase = true)) {
                    filters
                } else {
                    filters.copy(query = nextQuery)
                }
            }

            SpellsEvent.FavoritesToggled -> filters.copy(showFavorite = !filters.showFavorite)
            SpellsEvent.FiltersOpened -> filters.copy(showFilterDialog = true)
            is SpellsEvent.FiltersSubmitted -> filters.copy(
                showFilterDialog = false,
                currentClasses = if (event.classes == filters.currentClasses) {
                    filters.currentClasses
                } else {
                    event.classes
                },
            )

            is SpellsEvent.FiltersCanceled -> filters.copy(
                showFilterDialog = false,
                currentClasses = if (event.classes == filters.currentClasses) {
                    filters.currentClasses
                } else {
                    event.classes
                },
            )
        }

    private data class SpellFilters(
        val query: String = "",
        val showFavorite: Boolean = false,
        val showFilterDialog: Boolean = false,
        val currentClasses: Set<EntityRef> = emptySet(),
    )

    private data class SpellsQuery(
        val filters: SpellFilters,
        val allSpells: List<Spell>,
        val favoriteSpellIds: List<String>,
    ) {
        val query: String = filters.query
        val currentClasses: Set<EntityRef> = filters.currentClasses
        val showFavorite: Boolean = filters.showFavorite
    }

    private companion object {
        const val SELECTED_SECTION_KEY = "compendium_section"
    }
}
