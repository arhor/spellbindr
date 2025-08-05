package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.Choice
import com.github.arhor.spellbindr.ui.components.BaseScreen
import com.github.arhor.spellbindr.ui.components.NavButtons
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.BondsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.FlawsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.IdealsChanged
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationEvent.PersonalityTraitsChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameAndBackgroundScreen(
    onNext: () -> Unit,
    viewModel: CharacterCreationViewModel,
) {
    val state by viewModel.state.collectAsState()
    val background = state.background

    BaseScreen {
        OutlinedTextField(
            value = state.characterName,
            onValueChange = { viewModel.handleEvent(CharacterCreationEvent.NameChanged(it)) },
            label = { Text("Character Name") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = state.background?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Background") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (bg in state.backgrounds) {
                    DropdownMenuItem(
                        text = { Text(bg.name) },
                        onClick = {
                            viewModel.handleEvent(CharacterCreationEvent.BackgroundChanged(bg.name))
                            expanded = false
                        }
                    )
                }
            }
        }

        if (background != null) {
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
        }

        NavButtons(onNext = onNext)
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
