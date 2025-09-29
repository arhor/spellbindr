package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation
import com.github.arhor.spellbindr.utils.GenerationMethod
import com.github.arhor.spellbindr.utils.generate
import com.github.arhor.spellbindr.utils.roll4d6DropLowest
import com.github.arhor.spellbindr.utils.standardArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilitiesScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    viewModel: CharacterCreationViewModel
) {
    BaseScreenWithNavigation(
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 4 of 7: Abilities", style = MaterialTheme.typography.titleLarge)

        var selectedMethod by remember { mutableStateOf<GenerationMethod>(GenerationMethod.PointBuy) }

        val abilityScores = remember { mutableStateMapOf<String, Int>() }
        val rolledScores = remember { mutableStateListOf<Int>() }

        LaunchedEffect(selectedMethod) {
            when (val method = selectedMethod) {
                is GenerationMethod.Roll -> {
                    rolledScores.clear()
                    rolledScores.addAll(roll4d6DropLowest())
                    if (method.autoAssign) {
                        val scores = generate(method).scores
                        abilityScores.clear()
                        abilityScores.putAll(scores)
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    }
                }

                is GenerationMethod.StandardArray -> {
                    val scores = standardArray()
                    rolledScores.clear()
                    rolledScores.addAll(scores)
                }

                is GenerationMethod.PointBuy -> {
                    abilityScores.clear()
                    val initialScores = mapOf("STR" to 8, "DEX" to 8, "CON" to 8, "INT" to 8, "WIS" to 8, "CHA" to 8)
                    abilityScores.putAll(initialScores)
                    viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(initialScores))
                }
            }
        }
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedMethod.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Generation Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Point Buy") },
                    onClick = {
                        selectedMethod = GenerationMethod.PointBuy
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Standard Array") },
                    onClick = {
                        selectedMethod = GenerationMethod.StandardArray
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Roll") },
                    onClick = {
                        selectedMethod = GenerationMethod.Roll()
                        expanded = false
                    }
                )
            }
        }

        when (val method = selectedMethod) {
            is GenerationMethod.PointBuy -> {
                PointBuyUI(
                    scores = abilityScores,
                    onScoreChanged = { ability, score ->
                        abilityScores[ability] = score
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    }
                )
            }

            is GenerationMethod.StandardArray,
            is GenerationMethod.Roll -> {
                AssignmentUI(
                    rolledScores = rolledScores,
                    assignedScores = abilityScores,
                    onAssign = { ability, score ->
                        abilityScores[ability] = score
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    },
                    onReroll = if (method is GenerationMethod.Roll) {
                        {
                            rolledScores.clear()
                            rolledScores.addAll(roll4d6DropLowest())
                            abilityScores.clear()
                            viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(emptyMap()))
                        }
                    } else {
                        null
                    }
                )
            }
        }


    }
}
