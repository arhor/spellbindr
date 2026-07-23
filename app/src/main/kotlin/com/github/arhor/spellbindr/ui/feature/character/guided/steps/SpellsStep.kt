@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection

@Composable
internal fun SpellsStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val requirements = clazz?.let { computeSpellRequirements(it, state.preview) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Spells", style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                text = "MVP: only count limits are enforced. You can still edit spells later on the sheet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (clazz == null || requirements == null) {
            item {
                Text(
                    text = "This class doesn’t cast spells at level 1.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        if (state.spells.isEmpty()) {
            item {
                Text(
                    text = "Loading spells…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        val cantripsKey = GuidedCharacterSetupViewModel.spellCantripsChoiceKey()
        val spellsKey = GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()

        val selectedCantrips = state.selection.choiceSelections[cantripsKey].orEmpty()
        val selectedSpells = state.selection.choiceSelections[spellsKey].orEmpty()

        if (requirements.cantrips > 0) {
            item {
                Text(
                    text = "Cantrips: ${selectedCantrips.size} / ${requirements.cantrips}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                val options = remember(clazz.id, state.spells) {
                    state.spells
                        .asSequence()
                        .filter { spell -> spell.level == 0 && spell.classes.any { it.id == clazz.id } }
                        .sortedBy { it.name }
                        .associate { it.id to it.name }
                }
                ChoiceSection(
                    title = "Cantrips",
                    description = null,
                    choice = Choice.OptionsArrayChoice(
                        choose = requirements.cantrips,
                        from = options.keys.toList(),
                    ),
                    selected = selectedCantrips,
                    options = options,
                    onToggle = { spellId ->
                        onChoiceToggled(
                            cantripsKey,
                            spellId,
                            requirements.cantrips
                        )
                    },
                )
            }
        }

        if (requirements.level1Spells > 0) {
            item {
                Text(
                    text = "${requirements.level1Label.replaceFirstChar { it.uppercase() }}: "
                        + "${selectedSpells.size} / ${requirements.level1Spells}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item {
                val options = remember(clazz.id, state.spells) {
                    state.spells
                        .asSequence()
                        .filter { spell -> spell.level == 1 && spell.classes.any { it.id == clazz.id } }
                        .sortedBy { it.name }
                        .associate { it.id to it.name }
                }
                ChoiceSection(
                    title = requirements.level1Label.replaceFirstChar { it.uppercase() },
                    description = null,
                    choice = Choice.OptionsArrayChoice(
                        choose = requirements.level1Spells,
                        from = options.keys.toList(),
                    ),
                    selected = selectedSpells,
                    options = options,
                    onToggle = { spellId ->
                        onChoiceToggled(
                            spellsKey,
                            spellId,
                            requirements.level1Spells
                        )
                    },
                )
            }
        }
    }
}

