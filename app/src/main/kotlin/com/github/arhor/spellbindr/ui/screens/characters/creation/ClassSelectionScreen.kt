package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
fun ClassSelectionScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val selectedClass = state.characterClass
    val classes = state.classes
    val isLoading = state.isLoading
    val error = state.error

    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 3 of 9: Class Selection", style = MaterialTheme.typography.titleLarge)
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text("Error loading classes: $error", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                Text("Choose your class:", style = MaterialTheme.typography.titleMedium)
                classes.forEach { characterClass ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.handleEvent(CharacterCreationEvent.ClassSelection(characterClass)) },
                        colors = if (characterClass == selectedClass) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(characterClass.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

    }
}
