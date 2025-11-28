package com.github.arhor.spellbindr.ui.feature.dice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerIntent
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerState
import com.github.arhor.spellbindr.ui.feature.dice.model.canRollMain

private val QUICK_AMOUNT_DICE = listOf(4, 6, 8, 10, 12)

@Composable
fun MainDiceRollerCard(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Dice Roll",
                style = MaterialTheme.typography.titleMedium,
            )

            QuickDiceRow(state = state, onIntent = onIntent)

            CheckSection(state = state, onIntent = onIntent)

            AmountDiceSection(state = state, onIntent = onIntent)

            ActionRow(state = state, onIntent = onIntent)
        }
    }
}

@Preview
@Composable
private fun MainDiceRollerCardPreview() {
    val previewState = DiceRollerState(
        hasCheck = true,
        checkMode = CheckMode.ADVANTAGE,
        checkModifier = 3,
        amountDice = listOf(
            DiceGroup(sides = 8, count = 2),
            DiceGroup(sides = 6, count = 1),
        ),
    )
    MainDiceRollerCard(
        state = previewState,
        onIntent = {},
        modifier = Modifier.padding(16.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QuickDiceRow(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Quick dice",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            for (sides in QUICK_AMOUNT_DICE) {
                ElevatedAssistChip(
                    onClick = { onIntent(DiceRollerIntent.AddAmountDie(sides)) },
                    label = { Text(text = "d$sides") },
                )
            }

        }
        FilterChip(
            selected = state.hasCheck,
            onClick = { onIntent(DiceRollerIntent.ToggleCheck) },
            label = { Text(text = "d20 check") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Casino,
                    contentDescription = null,
                )
            },
            modifier = Modifier,
        )
    }
}

@Composable
private fun CheckSection(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//        Text(
//            text = "Check (d20)",
//            style = MaterialTheme.typography.titleSmall,
//        )
        if (state.hasCheck) {
            CheckModeSelector(
                selected = state.checkMode,
                onModeSelected = { onIntent(DiceRollerIntent.SetCheckMode(it)) },
            )
            ValueAdjuster(
                label = "Check modifier",
                value = state.checkModifier,
                onIncrement = { onIntent(DiceRollerIntent.IncrementCheckModifier) },
                onDecrement = { onIntent(DiceRollerIntent.DecrementCheckModifier) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckModeSelector(
    selected: CheckMode,
    onModeSelected: (CheckMode) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        CheckMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = CheckMode.entries.size),
            ) {
                Text(mode.shortLabel())
            }
        }
    }
}

@Composable
private fun AmountDiceSection(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Amount dice",
            style = MaterialTheme.typography.titleSmall,
        )
        if (state.amountDice.isEmpty()) {
            Text(
                text = "Tap d4, d6, d8, d10 or d12 above to add dice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.amountDice.forEach { group ->
                    DiceGroupRow(
                        group = group,
                        onIncrement = { onIntent(DiceRollerIntent.IncrementAmountDie(group.sides)) },
                        onDecrement = { onIntent(DiceRollerIntent.DecrementAmountDie(group.sides)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DiceGroupRow(
    group: DiceGroup,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${group.count}d${group.sides}",
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, enabled = group.count > 0) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease d${group.sides}",
                    )
                }
                IconButton(onClick = onIncrement) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase d${group.sides}",
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    state: DiceRollerState,
    onIntent: (DiceRollerIntent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { onIntent(DiceRollerIntent.ClearAll) }) {
            Text("Clear")
        }
        Button(
            onClick = { onIntent(DiceRollerIntent.RollMain) },
            enabled = state.canRollMain,
        ) {
            Text("Roll")
        }
    }
}
