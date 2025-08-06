package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.BaseScreenWithNavigation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilitiesScreen(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    viewModel: CharacterCreationViewModel
) {
    BaseScreenWithNavigation(
        padding = 24.dp,
        onPrev = onPrev,
        onNext = onNext
    ) {
        Text("Step 4 of 9: Abilities", style = MaterialTheme.typography.titleLarge)

        var selectedMethod by remember { mutableStateOf<AbilityScoreGenerator.GenerationMethod>(AbilityScoreGenerator.GenerationMethod.PointBuy) }

        val abilityScores = remember { mutableStateMapOf<String, Int>() }
        val rolledScores = remember { mutableStateListOf<Int>() }

        LaunchedEffect(selectedMethod) {
            when (val method = selectedMethod) {
                is AbilityScoreGenerator.GenerationMethod.Roll -> {
                    rolledScores.clear()
                    rolledScores.addAll(AbilityScoreGenerator.roll4d6DropLowest())
                    if (method.autoAssign) {
                        val scores = AbilityScoreGenerator.generate(method).scores
                        abilityScores.clear()
                        abilityScores.putAll(scores)
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    }
                }

                is AbilityScoreGenerator.GenerationMethod.StandardArray -> {
                    val scores = AbilityScoreGenerator.standardArray()
                    rolledScores.clear()
                    rolledScores.addAll(scores)
                }

                is AbilityScoreGenerator.GenerationMethod.PointBuy -> {
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
                    .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Point Buy") },
                    onClick = {
                        selectedMethod = AbilityScoreGenerator.GenerationMethod.PointBuy
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Standard Array") },
                    onClick = {
                        selectedMethod = AbilityScoreGenerator.GenerationMethod.StandardArray
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Roll") },
                    onClick = {
                        selectedMethod = AbilityScoreGenerator.GenerationMethod.Roll()
                        expanded = false
                    }
                )
            }
        }

        when (val method = selectedMethod) {
            is AbilityScoreGenerator.GenerationMethod.PointBuy -> {
                PointBuyUI(
                    scores = abilityScores,
                    onScoreChanged = { ability, score ->
                        abilityScores[ability] = score
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    }
                )
            }

            is AbilityScoreGenerator.GenerationMethod.StandardArray,
            is AbilityScoreGenerator.GenerationMethod.Roll -> {
                AssignmentUI(
                    rolledScores = rolledScores,
                    assignedScores = abilityScores,
                    onAssign = { ability, score ->
                        abilityScores[ability] = score
                        viewModel.handleEvent(CharacterCreationEvent.AbilityScoresChanged(abilityScores.toMap()))
                    },
                    onReroll = if (method is AbilityScoreGenerator.GenerationMethod.Roll) {
                        {
                            rolledScores.clear()
                            rolledScores.addAll(AbilityScoreGenerator.roll4d6DropLowest())
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
