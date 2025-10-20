package com.github.arhor.spellbindr.ui.feature.diceRoller.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.DiceRoll
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.DiceType
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.RollHistoryEntry
import com.github.arhor.spellbindr.ui.feature.diceRoller.model.RollSet
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RollHistory(
    rollHistory: List<RollHistoryEntry>,
    onDeleteRoll: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (rollHistory.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "No rolls yet. Start rolling some dice!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rollHistory.forEach { entry ->
            RollHistoryItem(
                entry = entry,
                onDelete = { onDeleteRoll(entry.id) }
            )
        }
    }
}

@Composable
private fun RollHistoryItem(
    entry: RollHistoryEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.rollSet.displayText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = entry.rollSet.individualResults,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete roll",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
private fun RollHistoryPreview() {
    SpellbindrTheme {
        RollHistory(
            rollHistory = listOf(
                RollHistoryEntry(
                    rollSet = RollSet(
                        diceType = DiceType.D20,
                        quantity = 1,
                        rolls = listOf(
                            DiceRoll(
                                diceType = DiceType.D20,
                                result = 15
                            )
                        ),
                        total = 20
                    )
                ),
                RollHistoryEntry(
                    rollSet = RollSet(
                        diceType = DiceType.D6,
                        quantity = 3,
                        rolls = listOf(
                            DiceRoll(
                                diceType = DiceType.D6,
                                result = 4
                            ),
                            DiceRoll(
                                diceType = DiceType.D6,
                                result = 2
                            ),
                            DiceRoll(
                                diceType = DiceType.D6,
                                result = 6
                            )
                        ),
                        total = 12
                    )
                )
            ),
            onDeleteRoll = {}
        )
    }
}
