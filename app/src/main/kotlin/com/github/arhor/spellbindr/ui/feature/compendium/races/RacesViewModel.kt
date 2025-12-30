package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.usecase.ObserveRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveTraitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Stable
@HiltViewModel
class RacesViewModel @Inject constructor(
    observeRacesUseCase: ObserveRacesUseCase,
    observeTraitsUseCase: ObserveTraitsUseCase,
) : ViewModel() {

    @Immutable
    data class RacesState(
        val races: List<Race> = emptyList(),
        val traits: Map<String, Trait> = emptyMap(),
        val expandedItemName: String? = null,
    )

    private val raceSelection = MutableStateFlow<String?>(null)

    val state = combine(
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

    fun onRaceClick(raceName: String) {
        raceSelection.update { current ->
            if (current == raceName) {
                null
            } else {
                raceName
            }
        }
    }
}
