package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait

sealed interface RacesUiState {
    @Immutable
    data object Loading : RacesUiState

    @Immutable
    data class Content(
        val races: List<Race> = emptyList(),
        val traits: Map<String, Trait> = emptyMap(),
        val selectedItemName: String? = null,
    ) : RacesUiState

    @Immutable
    data class Error(
        val errorMessage: String,
    ) : RacesUiState
}
