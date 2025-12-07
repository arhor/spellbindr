package com.github.arhor.spellbindr.ui.feature.dice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun LatestResultBar(
    latestResult: RollResult,
    onReRoll: () -> Unit,
    onShowDetails: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Latest Roll",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close latest roll",
                    )
                }
            }
            when (latestResult) {
                is RollResult.CheckAmountResult -> {
                    val hasCheck = latestResult.check != null
                    val hasAmount = latestResult.amount != null
                    latestResult.check?.let { CheckResultSummary(it) }
                    if (hasCheck && hasAmount) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    latestResult.amount?.let { AmountResultSummary(it) }
                }

                is RollResult.PercentileResult -> PercentileResultSummary(value = latestResult.value)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onShowDetails) {
                    Text("Details")
                }
                Button(onClick = onReRoll) {
                    Text("Re-roll")
                }
            }
        }
    }
}

@Composable
private fun CheckResultSummary(result: CheckResult) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "CHECK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = result.total.toString(),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = checkDetailText(result),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AmountResultSummary(result: AmountResult) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "AMOUNT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = result.total.toString(),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = amountDetailText(result),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PercentileResultSummary(value: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "PERCENTILE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "1d100 = $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun checkDetailText(result: CheckResult): String {
    val rollsText = result.rolls.joinToString(", ")
    val descriptor = when (result.mode) {
        CheckMode.NORMAL -> "1d20 ($rollsText)"
        CheckMode.ADVANTAGE -> "1d20 (Adv: $rollsText, kept ${result.keptRoll})"
        CheckMode.DISADVANTAGE -> "1d20 (Dis: $rollsText, kept ${result.keptRoll})"
    }
    val modifierText = formatSignedValue(result.modifier, showPlusForZero = true)
    return "$descriptor $modifierText = ${result.total}"
}

private fun amountDetailText(result: AmountResult): String {
    val groupsText = result.groups.joinToString(" + ") { it.summary() }
    return buildString {
        if (groupsText.isNotEmpty()) {
            append(groupsText)
            append(' ')
        }
        append(" = ")
        append(result.total)
    }
}

@Preview
@Composable
private fun LatestResultBarLightPreview() {
    LatestResultBarPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun LatestResultBarDarkPreview() {
    LatestResultBarPreview(isDarkTheme = true)
}

@Composable
private fun LatestResultBarPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        LatestResultBar(
            latestResult = RollResult.CheckAmountResult(
                check = CheckResult(
                    mode = CheckMode.ADVANTAGE,
                    rolls = listOf(17, 12),
                    modifier = 3,
                    keptRoll = 17,
                    total = 20,
                ),
                amount = AmountResult(
                    groups = listOf(
                        DiceGroupResult(sides = 6, rolls = listOf(4, 5)),
                        DiceGroupResult(sides = 8, rolls = listOf(6)),
                    ),
                    total = 15,
                ),
            ),
            onReRoll = {},
            onShowDetails = {},
            onClose = {},
        )
    }
}
