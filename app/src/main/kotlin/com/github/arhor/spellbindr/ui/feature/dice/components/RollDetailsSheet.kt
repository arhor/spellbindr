package com.github.arhor.spellbindr.ui.feature.dice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.dice.model.AmountResult
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckResult
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroupResult
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun RollDetailsSheetContent(
    result: RollResult,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Roll details",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close details",
                )
            }
        }

        when (result) {
            is RollResult.CheckAmountResult -> {
                result.check?.let {
                    CheckDetails(it)
                    if (result.amount != null) {
                        HorizontalDivider()
                    }
                }
                result.amount?.let { AmountDetails(it) }
            }

            is RollResult.PercentileResult -> PercentileDetails(value = result.value)
        }

        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun CheckDetails(check: CheckResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Check",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        DetailRow("Mode", check.mode.detailLabel())
        DetailRow("Dice rolled", check.rolls.joinToString(", "))
        if (check.mode != CheckMode.NORMAL) {
            DetailRow("Kept value", check.keptRoll.toString())
        }
        DetailRow("Modifier", formatSignedValue(check.modifier))
        DetailRow("Total", check.total.toString())
    }
}

@Composable
private fun AmountDetails(amount: AmountResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        if (amount.groups.isNotEmpty()) {
            amount.groups.forEachIndexed { index, group ->
                DiceGroupBreakdown(group)
                if (index != amount.groups.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else {
            Text(
                text = "No amount dice were rolled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DetailRow("Total", amount.total.toString())
    }
}

@Composable
private fun DiceGroupBreakdown(group: DiceGroupResult) {
    val rolls = group.rolls.joinToString(", ")
    DetailRow(
        label = "${group.count}d${group.sides}",
        value = "$rolls = ${group.subtotal}",
    )
}

@Composable
private fun PercentileDetails(value: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Percentile",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        DetailRow("Roll", "1d100 = $value")
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun CheckMode.detailLabel(): String = when (this) {
    CheckMode.NORMAL -> "Normal"
    CheckMode.ADVANTAGE -> "Advantage"
    CheckMode.DISADVANTAGE -> "Disadvantage"
}

@Preview
@Composable
private fun RollDetailsSheetLightPreview() {
    RollDetailsSheetPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun RollDetailsSheetDarkPreview() {
    RollDetailsSheetPreview(isDarkTheme = true)
}

@Composable
private fun RollDetailsSheetPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        RollDetailsSheetContent(
            result = RollResult.CheckAmountResult(
                check = CheckResult(
                    mode = CheckMode.NORMAL,
                    rolls = listOf(15),
                    modifier = 2,
                    keptRoll = 15,
                    total = 17,
                ),
                amount = AmountResult(
                    groups = listOf(DiceGroupResult(sides = 8, rolls = listOf(6, 5))),
                    total = 11,
                ),
            ),
            onClose = {},
        )
    }
}
