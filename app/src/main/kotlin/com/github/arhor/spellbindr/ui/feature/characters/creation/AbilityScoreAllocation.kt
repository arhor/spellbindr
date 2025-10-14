package com.github.arhor.spellbindr.ui.feature.characters.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.utils.PreviewScope
import com.github.arhor.spellbindr.utils.calculatePointBuyCost

@Composable
fun PointBuyUI(
    scores: Map<String, Int>,
    onScoreChanged: (String, Int) -> Unit
) {
    val pointCost = calculatePointBuyCost(scores)

    Column {
        Text("Points Remaining: ${27 - pointCost}")
        Spacer(modifier = Modifier.height(8.dp))
        scores.keys.forEach { ability ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(ability, modifier = Modifier.weight(1f))
                Button(onClick = {
                    if (scores.getOrDefault(ability, 8) > 8) {
                        onScoreChanged(ability, scores.getOrDefault(ability, 8) - 1)
                    }
                }) {
                    Text("-")
                }
                Text(scores.getOrDefault(ability, 8).toString())
                Button(onClick = {
                    if (scores.getOrDefault(ability, 8) < 15) {
                        onScoreChanged(ability, scores.getOrDefault(ability, 8) + 1)
                    }
                }) {
                    Text("+")
                }
            }
        }
    }
}

@Preview
@Composable
private fun PointBuyUIPreview() {
    PreviewScope {
        PointBuyUI(
            scores = mapOf("STR" to 8, "DEX" to 8, "CON" to 8, "INT" to 8, "WIS" to 8, "CHA" to 8),
            onScoreChanged = { _, _ -> }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentUI(
    rolledScores: List<Int>,
    assignedScores: Map<String, Int>,
    onAssign: (String, Int) -> Unit,
    onReroll: (() -> Unit)? = null,
) {
    val unassignedScores = rolledScores.toMutableList()
    assignedScores.values.forEach { unassignedScores.remove(it) }

    Column {
        if (onReroll != null) {
            Button(onClick = onReroll) {
                Text("Re-roll")
            }
        }
        Text("Rolled Scores: ${rolledScores.joinToString()}")
        Spacer(modifier = Modifier.height(8.dp))

        listOf("STR", "DEX", "CON", "INT", "WIS", "CHA").forEach { ability ->
            var expanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ability, modifier = Modifier.weight(1f))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = assignedScores[ability]?.toString() ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        unassignedScores.forEach { score ->
                            DropdownMenuItem(
                                text = { Text(score.toString()) },
                                onClick = {
                                    onAssign(ability, score)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
