package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Trait
import com.github.arhor.spellbindr.data.model.next.CharacterRace
import com.github.arhor.spellbindr.data.repository.RacesRepository
import com.github.arhor.spellbindr.data.repository.TraitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class RacesViewModel @Inject constructor(
    racesRepository: RacesRepository,
    traitsRepository: TraitsRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val races: List<CharacterRace> = emptyList(),
        val traits: Map<String, Trait> = emptyMap(),
        val expandedItemName: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                racesRepository.allRaces,
                traitsRepository.allTraits
            ) { races, traits ->
                val traitsMap = traits.associateBy { it.id }
                races to traitsMap
            }.collect { (races, traitsMap) ->
                _state.update { currentState ->
                    currentState.copy(
                        races = races,
                        traits = traitsMap
                    )
                }
            }
        }
    }

    fun handleRaceClick(raceName: String) {
        _state.update {
            it.copy(
                expandedItemName = if (it.expandedItemName == raceName) {
                    null
                } else {
                    raceName
                }
            )
        }
    }
}
