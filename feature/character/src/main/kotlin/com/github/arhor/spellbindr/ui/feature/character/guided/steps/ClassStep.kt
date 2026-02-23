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
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.displayName

@Composable
internal fun ClassStep(
    state: GuidedCharacterSetupUiState.Content,
    onClassSelected: (String) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val classes = remember(state.classes, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.classes
        } else {
            state.classes.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a class",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.classes.size >= 8) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search classes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        items(classes, key = { it.id }) { clazz ->
            val selected = state.selection.classId == clazz.id
            Card(
                onClick = { onClassSelected(clazz.id) },
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
                        Text(text = clazz.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        val savingThrows = clazz.savingThrows.joinToString(", ") { it.displayName() }
                        val skillPicks = clazz.proficiencyChoices.sumOf { choice ->
                            when (choice) {
                                is Choice.ProficiencyChoice ->
                                    if (choice.from.any { it.startsWith("skill-") }) choice.choose else 0

                                else -> 0
                            }
                        }
                        val spellcastingNote = if (clazz.spellcasting?.level == 1) {
                            "Spellcasting at level 1"
                        } else {
                            null
                        }
                        Text(
                            text = buildString {
                                append("Hit die: d")
                                append(clazz.hitDie)
                                append(" • Saves: ")
                                append(savingThrows)
                                if (skillPicks > 0) {
                                    append(" • Choose ")
                                    append(skillPicks)
                                    append(" skill")
                                    if (skillPicks != 1) append('s')
                                }
                                if (spellcastingNote != null) {
                                    append(" • ")
                                    append(spellcastingNote)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (state.classes.isNotEmpty() && classes.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

