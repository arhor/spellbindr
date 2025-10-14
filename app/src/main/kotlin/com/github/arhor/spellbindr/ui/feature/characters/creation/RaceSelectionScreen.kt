package com.github.arhor.spellbindr.ui.feature.characters.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun RaceSelectionScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val selectedRace = state.race
    val selectedSubrace = state.subrace
    val races = state.races
    val isLoading = state.isLoading
    val error = state.error

    val isSubraceRequired = selectedRace != null && (selectedRace.subraces?.isNotEmpty() == true)
    selectedRace != null && (!isSubraceRequired || selectedSubrace != null)

    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 2 of 7: Race", style = MaterialTheme.typography.titleLarge)
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
                    AnimatedVisibility(visible = race == selectedRace && race.subraces.isNotEmpty()) {
                        Column { // This Column is added to group subraces for AnimatedVisibility
                            race.subraces.forEach { subrace ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 16.dp) // Indent subraces
                                        .clickable {
                                            viewModel.handleEvent(CharacterCreationEvent.SubraceChanged(subrace.id))
                                        },
                                    colors = if (subrace == selectedSubrace) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors()
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(subrace.name, style = MaterialTheme.typography.titleMedium)
                                        // Optionally, display subrace traits here if they exist
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
