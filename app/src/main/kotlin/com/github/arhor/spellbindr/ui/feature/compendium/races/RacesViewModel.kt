package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
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
    private val observeRacesUseCase: ObserveRacesUseCase,
    private val observeTraitsUseCase: ObserveTraitsUseCase,
) : ViewModel() {

    private val expandedItemState = MutableStateFlow<String?>(null)
    private val racesState = observeRacesUseCase()
        .stateIn(viewModelScope, sharingStrategy, Loadable.Loading)
    private val traitsState = observeTraitsUseCase()
        .stateIn(viewModelScope, sharingStrategy, Loadable.Loading)

    val uiState: StateFlow<RacesUiState> = combine(
        racesState,
        traitsState,
        expandedItemState,
        ::toUiState,
    ).stateIn(viewModelScope, sharingStrategy, RacesUiState.Loading)

    fun onRaceClick(raceName: String) {
        expandedItemState.update { current ->
            if (current == raceName) null else raceName
        }
    }

    private fun toUiState(
        races: Loadable<List<Race>>,
        traits: Loadable<List<Trait>>,
        expandedItemName: String?
    ): RacesUiState {
        return when {
            races is Loadable.Loading || traits is Loadable.Loading -> {
                RacesUiState.Loading
            }

            races is Loadable.Error -> {
                RacesUiState.Error(races.cause?.message ?: "Failed to load races")
            }

            traits is Loadable.Error -> {
                RacesUiState.Error(traits.cause?.message ?: "Failed to load traits")
            }

            races is Loadable.Ready && traits is Loadable.Ready -> {
                RacesUiState.Content(
                    races = races.data,
                    traits = traits.data.associateBy(Trait::id),
                    selectedItemName = expandedItemName,
                )
            }

            else -> {
                RacesUiState.Loading
            }
        }
    }

    companion object {
        private val sharingStrategy = SharingStarted.WhileSubscribed(5_000)
    }
}
