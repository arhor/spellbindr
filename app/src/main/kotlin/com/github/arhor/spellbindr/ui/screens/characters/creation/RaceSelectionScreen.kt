package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RaceSelectionScreen(
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val selectedRace = state.race
    val selectedSubrace = state.subrace
    val races = state.races
    val isLoading = state.isLoading
    val error = state.error

    val subraces = selectedRace?.subraces ?: emptyList()
    val isSubraceRequired = selectedRace != null && subraces.isNotEmpty()
    val isNextEnabled = selectedRace != null && (!isSubraceRequired || selectedSubrace != null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 2 of 9: Race Selection", style = MaterialTheme.typography.titleLarge)
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("Error loading races: $error", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                Text("Choose your race:", style = MaterialTheme.typography.titleMedium)
                races.forEach { race ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.handleEvent(CharacterCreationEvent.RaceChanged(race.id))
                            },
                        colors = if (race == selectedRace) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(race.name, style = MaterialTheme.typography.titleMedium)
                            if (race.traits.isNotEmpty()) {
                                Text(
                                    race.traits.joinToString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                if (isSubraceRequired) {
                    Spacer(Modifier.height(16.dp))
                    Text("Choose a subrace:", style = MaterialTheme.typography.titleMedium)
                    subraces.forEach { subrace ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.handleEvent(CharacterCreationEvent.SubraceChanged(subrace.id))
                                },
                            colors = if (subrace == selectedSubrace) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(subrace.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            enabled = isNextEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}
