@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.abbreviation
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.StandardArray

@Composable
internal fun StandardArrayAssign(
    assignments: Map<AbilityId, Int?>,
    onStandardArrayAssigned: (AbilityId, Int?) -> Unit,
) {
    var selectedScore by rememberSaveable { mutableStateOf<Int?>(null) }
    val assignedScores = assignments.values.filterNotNull().toSet()
    val availableScores = StandardArray.filter { it !in assignedScores }
    val assignedCount = assignedScores.size

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (availableScores.isEmpty()) {
            Text(
                text = "All standard-array scores are assigned.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$assignedCount of ${AbilityIds.standardOrder.size} assigned",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (selectedScore == null) {
                        "Choose an available score, then choose an ability."
                    } else {
                        "$selectedScore selected — choose an ability to assign it."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableScores.forEach { score ->
                    FilterChip(
                        selected = selectedScore == score,
                        onClick = {
                            selectedScore = if (selectedScore == score) null else score
                        },
                        label = { Text(score.toString()) },
                        modifier = Modifier.semantics {
                            contentDescription = if (selectedScore == score) {
                                "$score selected. Tap to cancel selection."
                            } else {
                                "$score available. Tap to select."
                            }
                        },
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                AbilityIds.standardOrder.forEachIndexed { index, abilityId ->
                    val score = assignments[abilityId]
                    AbilityAssignmentRow(
                        abilityId = abilityId,
                        score = score,
                        selectedScore = selectedScore,
                        onClick = {
                            when {
                                selectedScore != null -> {
                                    onStandardArrayAssigned(abilityId, selectedScore)
                                    selectedScore = null
                                }

                                score != null -> {
                                    onStandardArrayAssigned(abilityId, null)
                                    selectedScore = score
                                }
                            }
                        },
                    )
                    if (index < AbilityIds.standardOrder.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AbilityAssignmentRow(
    abilityId: AbilityId,
    score: Int?,
    selectedScore: Int?,
    onClick: () -> Unit,
) {
    val modifier = score?.let { Math.floorDiv(it - 10, 2) }
    val action = when {
        selectedScore != null -> "Tap to assign $selectedScore."
        score != null -> "Tap to pick up $score."
        else -> "Select an available score to assign it."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = selectedScore != null || score != null, onClick = onClick)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = buildString {
                    append(abilityId.displayName())
                    append(": ")
                    if (score == null) append("unassigned") else append("$score, modifier ${formatModifier(modifier!!)}")
                    append(". $action")
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = abilityId.abbreviation(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = abilityId.displayName(), style = MaterialTheme.typography.titleSmall)
            Text(
                text = if (score == null) "Unassigned" else "Modifier ${formatModifier(modifier!!)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = score?.toString() ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            color = if (score == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        )
    }
}

private fun formatModifier(modifier: Int): String = if (modifier >= 0) "+$modifier" else modifier.toString()
