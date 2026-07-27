package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.guided.components.race.RaceCarousel

@Composable
internal fun RaceStep(
    state: GuidedCharacterSetupUiState.Content,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Choose a race",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = "Swipe to explore, then select a race. You can review every trait in Details.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
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
            modifier = Modifier.weight(1f),
        )
    }
}
