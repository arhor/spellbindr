package com.github.arhor.spellbindr.ui.feature.compendium

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Alignment
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.Trait
import com.github.arhor.spellbindr.data.model.next.CharacterRace
import com.github.arhor.spellbindr.data.model.predefined.Condition
import com.github.arhor.spellbindr.data.repository.AlignmentRepository
import com.github.arhor.spellbindr.data.repository.CharacterClassRepository
import com.github.arhor.spellbindr.data.repository.RacesRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import com.github.arhor.spellbindr.data.repository.TraitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Stable
@HiltViewModel
class CompendiumViewModel @Inject constructor(
    private val alignmentRepository: AlignmentRepository,
    private val characterClassRepository: CharacterClassRepository,
    private val racesRepository: RacesRepository,
    private val spellRepository: SpellRepository,
    private val traitsRepository: TraitsRepository,
) : ViewModel() {

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
        override val spells: List<Spell> = emptyList(),
        override val showFavorite: Boolean = false,
        override val showFilterDialog: Boolean = false,
        override val castingClasses: List<EntityRef> = emptyList(),
        override val currentClasses: Set<EntityRef> = emptySet(),
        override val isLoading: Boolean = false,
        override val error: String? = null,
    ) : SpellListState

    @Immutable
    data class State(
        val alignmentsState: AlignmentsState = AlignmentsState(),
        val conditionsState: ConditionsState = ConditionsState(),
        val racesState: RacesState = RacesState(),
        val spellsState: SpellsState = SpellsState(),
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            alignmentRepository.allAlignments.collect { data ->
                _state.update {
                    it.copy(
                        alignmentsState = it.alignmentsState.copy(
                            alignments = data,
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            characterClassRepository
                .findSpellcastingClassesRefs()
                .let { refs ->
                    _state.update {
                        it.copy(
                            spellsState = it.spellsState.copy(
                                castingClasses = refs,
                            )
                        )
                    }
                }
        }
        viewModelScope.launch {
            combine(racesRepository.allRaces, traitsRepository.allTraits, ::Pair)
                .collect { (races, traits) ->
                    _state.update {
                        it.copy(
                            racesState = it.racesState.copy(
                                races = races,
                                traits = traits.associateBy(Trait::id),
                            )
                        )
                    }
                }
        }
        observeStateChanges()
    }

    fun handleAlignmentClick(alignmentName: String) {
        _state.update {
            it.copy(
                alignmentsState = it.alignmentsState.copy(
                    expandedItemName = if (it.alignmentsState.expandedItemName == alignmentName) {
                        null
                    } else {
                        alignmentName
                    }
                )
            )
        }
    }

    fun onFavoritesClicked() {
        _state.update {
            it.copy(
                spellsState = it.spellsState.copy(
                    showFavorite = !it.spellsState.showFavorite
                )
            )
        }
    }

    fun onFilterClicked() {
        _state.update {
            it.copy(
                spellsState = it.spellsState.copy(
                    showFilterDialog = true
                )
            )
        }
    }

    fun onQueryChanged(query: String) {
        val currQuery = _state.value.spellsState.query
        val nextQuery = query.trim()

        if (!nextQuery.equals(currQuery, ignoreCase = true)) {
            _state.update {
                it.copy(
                    spellsState = it.spellsState.copy(
                        query = nextQuery
                    )
                )
            }
        }
    }

    fun onFilterChanged(classes: Set<EntityRef>) {
        _state.update {
            if (classes != _state.value.spellsState.currentClasses) {
                it.copy(
                    spellsState = it.spellsState.copy(
                        showFilterDialog = false,
                        currentClasses = classes,
                    )
                )
            } else {
                it.copy(
                    spellsState = it.spellsState.copy(
                        showFilterDialog = false,
                    )
                )
            }
        }
    }

    fun handleConditionClick(condition: Condition) {
        _state.update {
            it.copy(
                conditionsState = it.conditionsState.copy(
                    expandedItem = if (it.conditionsState.expandedItem == condition) {
                        null
                    } else {
                        condition
                    }
                )

            )
        }
    }

    fun handleRaceClick(raceName: String) {
        _state.update {
            it.copy(
                racesState = it.racesState.copy(
                    expandedItemName = if (it.racesState.expandedItemName == raceName) {
                        null
                    } else {
                        raceName
                    }
                )
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeStateChanges() {
        viewModelScope.launch {
            combine(_state, spellRepository.allSpells, spellRepository.favSpells, ::toObservableData)
                .debounce(350.milliseconds)
                .distinctUntilChanged()
                .collect { data ->
                    try {
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    isLoading = true,
                                    error = null
                                )
                            )
                        }
                        val spells = spellRepository.findSpells(
                            query = data.query,
                            classes = data.currentClasses,
                            favorite = data.showFavorite,
                        )
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    spells = spells,
                                    isLoading = false
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, e.message.toString(), e)
                        _state.update {
                            it.copy(
                                spellsState = it.spellsState.copy(
                                    error = "Oops, something went wrong...",
                                    isLoading = false,
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun toObservableData(
        state: State, allSpells: List<Spell>, favSpells: List<String>
    ): ObservableData = ObservableData(
        state.spellsState.query,
        state.spellsState.castingClasses,
        state.spellsState.currentClasses,
        state.spellsState.showFavorite,
        allSpells,
        favSpells,
    )

    private data class ObservableData(
        val query: String,
        val castingClasses: List<EntityRef>,
        val currentClasses: Set<EntityRef>,
        val showFavorite: Boolean,
        val allSpells: List<Spell>,
        val favSpells: List<String>,
    )

    companion object {
        private val TAG = this::class.java.enclosingClass.simpleName
    }
}
