package com.github.arhor.spellbindr.ui.feature.diceRoller.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.DiceType
import com.github.arhor.spellbindr.data.model.RollSet
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme

@Composable
fun RollResult(
    rollSet: RollSet?,
    modifier: Modifier = Modifier
) {
    if (rollSet == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Last Roll: ${rollSet.displayText}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Individual Results:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rollSet.rolls.forEach { roll ->
                    DiceResultChip(
                        result = roll.result,
                        isCriticalHit = roll.isCriticalHit,
                        isCriticalMiss = roll.isCriticalMiss
                    )
                }
            }
        }
    }
}

@Composable
private fun DiceResultChip(
    result: Int,
    isCriticalHit: Boolean,
    isCriticalMiss: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCriticalHit -> Color(0xFF4CAF50) // Green for critical hit
        isCriticalMiss -> Color(0xFFF44336) // Red for critical miss
        else -> MaterialTheme.colorScheme.primary
    }

    val textColor = when {
        isCriticalHit || isCriticalMiss -> Color.White
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = result.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RollResultPreview() {
    SpellbindrTheme {
        RollResult(
            rollSet = RollSet(
                diceType = DiceType.D20,
                quantity = 3,
                rolls = listOf(
                    com.github.arhor.spellbindr.data.model.DiceRoll(
                        diceType = DiceType.D20,
                        result = 20,
                        isCriticalHit = true
                    ),
                    com.github.arhor.spellbindr.data.model.DiceRoll(
                        diceType = DiceType.D20,
                        result = 15,
                        isCriticalHit = false
                    ),
                    com.github.arhor.spellbindr.data.model.DiceRoll(
                        diceType = DiceType.D20,
                        result = 1,
                        isCriticalMiss = true
                    )
                ),
                total = 37
            )
        )
    }
}
