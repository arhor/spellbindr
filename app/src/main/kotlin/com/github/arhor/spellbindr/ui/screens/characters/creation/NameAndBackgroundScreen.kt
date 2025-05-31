package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

private val backgrounds = listOf(
    "Acolyte",
    "Charlatan",
    "Criminal",
    "Entertainer",
    "Folk Hero",
    "Guild Artisan",
    "Hermit",
    "Noble",
    "Outlander",
    "Sage",
    "Sailor",
    "Soldier",
    "Urchin"
)
private val alignments = listOf(
    "Lawful Good", "Neutral Good", "Chaotic Good",
    "Lawful Neutral", "True Neutral", "Chaotic Neutral",
    "Lawful Evil", "Neutral Evil", "Chaotic Evil"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameAndBackgroundScreen(
    onNext: () -> Unit = {},
    viewModel: CharacterCreationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var backgroundExpanded = remember { mutableStateOf(false) }
    var alignmentExpanded = remember { mutableStateOf(false) }

    val isNextEnabled = state.name.isNotBlank() && state.background.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Step 1 of 9: Name & Background", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Character Name*") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(
            expanded = backgroundExpanded.value,
            onExpandedChange = { backgroundExpanded.value = !backgroundExpanded.value }
        ) {
            OutlinedTextField(
                value = state.background,
                onValueChange = {},
                readOnly = true,
                label = { Text("Background*") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = backgroundExpanded.value) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = backgroundExpanded.value,
                onDismissRequest = { backgroundExpanded.value = false }
            ) {
                backgrounds.forEach { bg ->
                    DropdownMenuItem(
                        text = { Text(bg) },
                        onClick = {
                            viewModel.onBackgroundChanged(bg)
                            backgroundExpanded.value = false
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = alignmentExpanded.value,
            onExpandedChange = { alignmentExpanded.value = !alignmentExpanded.value }
        ) {
            OutlinedTextField(
                value = state.alignment ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Alignment (optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alignmentExpanded.value) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = alignmentExpanded.value,
                onDismissRequest = { alignmentExpanded.value = false }
            ) {
                alignments.forEach { align ->
                    DropdownMenuItem(
                        text = { Text(align) },
                        onClick = {
                            viewModel.onAlignmentChanged(align)
                            alignmentExpanded.value = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = state.backstory ?: "",
            onValueChange = { viewModel.onBackstoryChanged(it) },
            label = { Text("Backstory (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5,
            singleLine = false
        )
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
