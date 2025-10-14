package com.github.arhor.spellbindr.ui.feature.characters.creation

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation

@Composable
fun SummaryScreen(
    onPrev: () -> Unit,
    onFinish: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()

    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = if (state.isCharacterComplete) {
            {
                viewModel.handleEvent(CharacterCreationEvent.SaveCharacter)
                onFinish()
            }
        } else {
            null
        },
        nextText = "Done"
    ) {
        Text("Step 7 of 7: Summary", style = MaterialTheme.typography.titleLarge)

        SummaryItem(label = "Name", value = state.characterName.ifBlank { " - " })
        (state.subrace?.name ?: state.race?.name)?.let {
            SummaryItem(label = "Race", value = it)
        }

        SummaryItem(label = "Class", value = state.characterClass?.name)
        SummaryItem(label = state.subclassSelectionLabel ?: "Subclass", value = state.characterSubclass?.name)
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
