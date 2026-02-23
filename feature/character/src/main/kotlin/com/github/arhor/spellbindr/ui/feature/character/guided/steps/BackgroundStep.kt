@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection

@Composable
internal fun BackgroundStep(
    state: GuidedCharacterSetupUiState.Content,
    onBackgroundSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val backgrounds = remember(state.backgrounds, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.backgrounds
        } else {
            state.backgrounds.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a background",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.backgrounds.size >= 10) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search backgrounds") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        items(backgrounds, key = { it.id }) { entry ->
            val selected = state.selection.backgroundId == entry.id
            Card(
                onClick = { onBackgroundSelected(entry.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(text = entry.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        val featureSummary = entry.feature.desc.firstOrNull().orEmpty()
                        Text(
                            text = "Feature: ${entry.feature.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (featureSummary.isNotBlank()) {
                            Text(
                                text = featureSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (state.backgrounds.isNotEmpty() && backgrounds.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val languageChoice = bg?.languageChoice
        if (bg != null && languageChoice != null) {
            item {
                val choiceKey = GuidedCharacterSetupViewModel.backgroundLanguageChoiceKey()
                val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                val options =
                    remember(languageChoice, state.referenceDataVersion) { resolveOptions(languageChoice, state) }
                ChoiceSection(
                    title = "Languages",
                    description = "Choose ${languageChoice.choose}",
                    choice = languageChoice,
                    selected = selected,
                    options = options,
                    disabledOptions = computeAlreadySelectedLanguageReasons(state, choiceKey),
                    onToggle = { optionId ->
                        onChoiceToggled(
                            choiceKey,
                            optionId,
                            languageChoice.choose
                        )
                    },
                )
            }
        }
    }
}

