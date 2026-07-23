package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class RacesViewModel @Inject constructor(
    private val observeRaces: ObserveAllRacesUseCase,
    private val observeTraits: ObserveAllTraitsUseCase,
) : ViewModel() {

    private data class State(
        val selectedItemId: String? = null,
    )

    private val _state = MutableStateFlow(State())

    val uiState: StateFlow<RacesUiState> = combine(
        _state,
        observeRaces(),
        observeTraits(),
    ) { state, races, traits ->
        when {
            races is Loadable.Content && traits is Loadable.Content -> {
                RacesUiState.Content(
                    races = races.data,
                    traits = traits.data.associateBy(Trait::id),
                    selectedItemId = state.selectedItemId,
                )
            }

            races is Loadable.Failure -> {
                RacesUiState.Failure(races.errorMessage ?: "Failed to load races")
            }

            traits is Loadable.Failure -> {
                RacesUiState.Failure(traits.errorMessage ?: "Failed to load traits")
            }

            else -> {
                RacesUiState.Loading
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RacesUiState.Loading)

    fun dispatch(intent: RacesIntent) {
        when (intent) {
            is RacesIntent.RaceClicked -> toggleSelection(intent.raceId)
        }
    }

    private fun toggleSelection(raceId: String) {
        _state.update { state ->
            state.copy(
                selectedItemId = raceId.takeIf { it != state.selectedItemId }
            )
        }
    }
}
