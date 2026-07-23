@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.ui.feature.character.guided.components.SummaryRow
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationIssue
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedValidationResult

@Composable
internal fun ReviewStep(
    state: GuidedCharacterSetupUiState.Content,
    validation: GuidedValidationResult,
    onGoToStep: (GuidedStep) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val subclassName = state.selection.subclassId?.let { id -> clazz?.subclasses?.firstOrNull { it.id == id }?.name }
    val race = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }
    val subraceName = state.selection.subraceId?.let { id -> race?.subraces?.firstOrNull { it.id == id }?.name }
    val background = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Review", style = MaterialTheme.typography.titleMedium) }

        item {
            val preview = state.preview
            val abilitiesLine = AbilityIds.standardOrder.joinToString(" • ") { abilityId ->
                val score = preview.abilityScores.scoreFor(abilityId)
                val mod = preview.abilityScores.modifierFor(abilityId)
                val modLabel = if (mod >= 0) "+$mod" else mod.toString()
                "${abilityId.uppercase()} $score ($modLabel)"
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = "Quick stats", style = MaterialTheme.typography.titleSmall)
                    Text(text = abilitiesLine, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "HP ${preview.maxHitPoints} • AC ${preview.armorClass} • Speed ${preview.speed} ft",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Languages: ${preview.languagesCount} • Proficiencies: ${preview.proficienciesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text(text = "Jump back to edit", style = MaterialTheme.typography.titleSmall)
        }

        item {
            SummaryRow(
                label = "Name",
                value = state.name.ifBlank { "—" },
                onClick = { onGoToStep(GuidedStep.BASICS) },
            )
        }
        item {
            SummaryRow(
                label = "Class",
                value = clazz?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.CLASS) },
            )
        }
        if (GuidedStep.CLASS_CHOICES in state.steps) {
            item {
                SummaryRow(
                    label = "Subclass & choices",
                    value = subclassName ?: "—",
                    onClick = { onGoToStep(GuidedStep.CLASS_CHOICES) },
                )
            }
        }
        item {
            SummaryRow(
                label = "Race",
                value = race?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.RACE) },
            )
        }
        if (!subraceName.isNullOrBlank()) {
            item {
                SummaryRow(
                    label = "Subrace",
                    value = subraceName,
                    onClick = { onGoToStep(GuidedStep.RACE) },
                )
            }
        }
        item {
            SummaryRow(
                label = "Background",
                value = background?.name ?: "—",
                onClick = { onGoToStep(GuidedStep.BACKGROUND) },
            )
        }
        item {
            SummaryRow(
                label = "Ability scores",
                value = state.selection.abilityMethod?.label ?: "—",
                onClick = { onGoToStep(GuidedStep.ABILITY_ASSIGN) },
            )
        }
        item {
            SummaryRow(
                label = "Skills & proficiencies",
                value = "Tap to edit",
                onClick = { onGoToStep(GuidedStep.SKILLS_PROFICIENCIES) },
            )
        }
        item {
            SummaryRow(
                label = "Equipment",
                value = "Tap to edit",
                onClick = { onGoToStep(GuidedStep.EQUIPMENT) },
            )
        }
        if (GuidedStep.SPELLS in state.steps) {
            item {
                SummaryRow(
                    label = "Spells",
                    value = "Tap to edit",
                    onClick = { onGoToStep(GuidedStep.SPELLS) },
                )
            }
        }

        if (validation.issues.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = "Checks", style = MaterialTheme.typography.titleSmall)
                        validation.issues.forEach { issue ->
                            val prefix = if (issue.severity == GuidedValidationIssue.Severity.ERROR) {
                                "Failure"
                            } else {
                                "Note"
                            }
                            Text(text = "$prefix: ${issue.message}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "All set.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

