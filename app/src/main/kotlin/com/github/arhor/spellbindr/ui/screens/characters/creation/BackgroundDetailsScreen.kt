package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.common.Choice
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.BondsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.FlawsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.IdealsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.PersonalityTraitsChanged

@Composable
fun BackgroundDetailsScreen(
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val background = state.background

    if (background == null) {
        // Handle case where background is not selected
        Text("Please select a background first.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Background Details", style = MaterialTheme.typography.titleLarge)

        ChoiceSection(
            title = "Personality Traits",
            choice = background.personalityTraits,
            selection = state.selectedPersonalityTraits,
            onSelectionChanged = { viewModel.handleEvent(PersonalityTraitsChanged(it)) }
        )

        ChoiceSection(
            title = "Ideals",
            choice = background.ideals,
            selection = state.selectedIdeals,
            onSelectionChanged = { viewModel.handleEvent(IdealsChanged(it)) }
        )

        ChoiceSection(
            title = "Bonds",
            choice = background.bonds,
            selection = state.selectedBonds,
            onSelectionChanged = { viewModel.handleEvent(BondsChanged(it)) }
        )

        ChoiceSection(
            title = "Flaws",
            choice = background.flaws,
            selection = state.selectedFlaws,
            onSelectionChanged = { viewModel.handleEvent(FlawsChanged(it)) }
        )

        Button(onClick = onNext) {
            Text("Next")
        }
    }
}

@Composable
private fun ChoiceSection(
    title: String,
    choice: Choice,
    selection: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)

        when (choice) {
            is Choice.OptionsArrayChoice -> {
                // For simplicity, we'll use a text field to enter comma-separated values
                OutlinedTextField(
                    value = selection.joinToString(),
                    onValueChange = { onSelectionChanged(it.split(",").map(String::trim)) },
                    label = { Text("Enter ${choice.choose} choices, separated by commas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                Text(text = "Unsupported choice type")
            }
        }
    }
} 
