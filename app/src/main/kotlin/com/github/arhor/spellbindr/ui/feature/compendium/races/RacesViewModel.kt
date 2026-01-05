package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveTraitsUseCase
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
    private val observeRaces: ObserveRacesUseCase,
    private val observeTraits: ObserveTraitsUseCase,
) : ViewModel() {

    private val selectedItemIdState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RacesUiState> = combine(
        observeRaces(),
        observeTraits(),
        selectedItemIdState,
    ) { races, traits, expandedItemId ->
        when {
            races is Loadable.Ready && traits is Loadable.Ready -> {
                RacesUiState.Content(
                    races = races.data,
                    traits = traits.data.associateBy(Trait::id),
                    selectedItemId = expandedItemId,
                )
            }

            races is Loadable.Error -> {
                RacesUiState.Error(races.errorMessage ?: "Failed to load races")
            }

            traits is Loadable.Error -> {
                RacesUiState.Error(traits.errorMessage ?: "Failed to load traits")
            }

            else -> {
                RacesUiState.Loading
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RacesUiState.Loading)

    fun onRaceClick(raceId: String) {
        selectedItemIdState.update {
            if (it != raceId) {
                raceId
            } else {
                null
            }
        }
    }
}
