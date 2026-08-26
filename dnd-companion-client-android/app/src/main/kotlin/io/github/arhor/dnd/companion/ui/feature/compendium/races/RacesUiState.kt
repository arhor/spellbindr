package io.github.arhor.dnd.companion.ui.feature.compendium.races

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.Race
import io.github.arhor.dnd.companion.domain.model.Trait

sealed interface RacesUiState {
    @Immutable
    data object Loading : RacesUiState

    @Immutable
    data class Content(
        val races: List<Race> = emptyList(),
        val traits: Map<String, Trait> = emptyMap(),
        val selectedItemId: String? = null,
    ) : RacesUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : RacesUiState
}
