@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlin.random.Random

@Composable
fun DiceScreen(
    modifier: Modifier = Modifier,
) {
    val dicePool = remember { mutableStateMapOf<DiceType, Int>() }
    val history = remember { mutableStateListOf<DiceRollResult>() }

    fun addDie(type: DiceType) {
        dicePool[type] = dicePool.getOrDefault(type, 0) + 1
    }

    fun clearPool() {
        dicePool.clear()
    }

    fun rollDice() {
        if (dicePool.isEmpty()) return
        val breakdown = dicePool.mapValues { (type, count) ->
            List(count) { Random.nextInt(1, type.sides + 1) }
        }
        val total = breakdown.values.flatten().sum()
        history.add(
            0,
            DiceRollResult(
                id = System.currentTimeMillis(),
                total = total,
                breakdown = breakdown,
            )
        )
    }

    val poolDescription = dicePool.entries
        .sortedBy { it.key.ordinal }
        .joinToString(" + ") { (type, count) -> "${count}d${type.sides}" }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        TopAppBar(title = { Text("Dice") })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Add dice to your pool",
                style = MaterialTheme.typography.titleMedium,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(DiceType.entries) { type ->
                    FilledTonalButton(onClick = { addDie(type) }) {
                        Text(type.label)
                    }
                }
            }
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (poolDescription.isBlank()) {
                            "Tap a die to build a roll."
                        } else {
                            poolDescription
                        },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            onClick = ::rollDice,
                            enabled = dicePool.isNotEmpty(),
                        ) {
                            Text("Roll")
                        }
                        TextButton(
                            onClick = ::clearPool,
                            enabled = dicePool.isNotEmpty(),
                        ) {
                            Text("Clear pool")
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Recent rolls",
                style = MaterialTheme.typography.titleMedium,
            )

            if (history.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = "Results will appear here. Rolls are kept on device only.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(history, key = { it.id }) { result ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${result.total} total",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                result.breakdown.forEach { (type, rolls) ->
                                    Text(
                                        text = "${rolls.size}d${type.sides}: ${rolls.joinToString()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class DiceType(val label: String, val sides: Int) {
    D4("d4", 4),
    D6("d6", 6),
    D8("d8", 8),
    D10("d10", 10),
    D12("d12", 12),
    D20("d20", 20),
    D100("d100", 100),
}

private data class DiceRollResult(
    val id: Long,
    val total: Int,
    val breakdown: Map<DiceType, List<Int>>,
)

@Preview
@Composable
private fun DiceScreenPreview() {
    AppTheme {
        DiceScreen()
    }
}
