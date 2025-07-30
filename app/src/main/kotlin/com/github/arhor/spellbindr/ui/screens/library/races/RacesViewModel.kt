package com.github.arhor.spellbindr.ui.screens.library.races

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.data.model.Race
import com.github.arhor.spellbindr.data.repository.RacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class RacesViewModel @Inject constructor(
    racesRepository: RacesRepository,
) : ViewModel() {

    @Immutable
    data class State(
        val races: List<Race> = emptyList(),
        val expandedItemName: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            racesRepository.allRaces.collect { data ->
                _state.update { it.copy(races = data) }
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