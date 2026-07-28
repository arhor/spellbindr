package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.arhor.spellbindr.ui.feature.character.guided.components.race.RaceCarousel

@Composable
internal fun RaceStep(
    state: GuidedCharacterSetupUiState.Content,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
) {
    RaceCarousel(
        races = state.races,
        traitsById = state.traitsById,
        selectedRaceId = state.selection.raceId,
        selectedSubraceId = state.selection.subraceId,
        onRaceSelected = onRaceSelected,
        onSubraceSelected = { raceId, subraceId ->
            if (state.selection.raceId != raceId) onRaceSelected(raceId)
            onSubraceSelected(subraceId)
        },
        modifier = Modifier.fillMaxSize(),
    )
}
