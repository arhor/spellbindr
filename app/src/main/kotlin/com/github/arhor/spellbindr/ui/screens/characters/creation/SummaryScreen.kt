package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummaryScreen(
    onFinish: () -> Unit,
    viewModel: CharacterCreationViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Step 9 of 9: Summary", style = MaterialTheme.typography.titleLarge)

        SummaryItem(label = "Name", value = state.characterName)
        SummaryItem(label = "Race", value = state.race?.name)
        SummaryItem(label = "Subrace", value = state.subrace?.name)
        SummaryItem(label = "Class", value = state.characterClass?.name)
        SummaryItem(label = "Background", value = state.background?.name)

        if (!state.isCharacterComplete) {
            Text(
                text = "Please complete the following sections:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            for (missing in state.requiredSelections) {
                Text(text = "- $missing", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.handleEvent(CharacterCreationEvent.SaveCharacter)
                onFinish()
            },
            enabled = state.isCharacterComplete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String?) {
    if (value != null) {
        Row {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(2f)
            )
        }
        HorizontalDivider()
    }
}
