package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlin.math.hypot
import kotlin.math.min

@Immutable
data class AbilityTokenData(
    val abbreviation: String,
    val score: Int,
    val modifier: Int,
    val savingThrowBonus: Int,
    val proficient: Boolean = false,
)

@Composable
fun AbilityTokensGrid(
    abilities: List<AbilityTokenData>,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 12.dp,
    verticalSpacing: Dp = 12.dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        abilities.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            ) {
                row.forEach { ability ->
                    AbilityOctagonTile(
                        ability = ability,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f),
                    )
                }
                repeat(3 - row.size) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.9f),
                    )
                }
            }
        }
    }
}

@Composable
fun AbilityOctagonTile(
    ability: AbilityTokenData,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 12.dp),
        ) {
            Text(
                text = ability.abbreviation,
                style = MaterialTheme.typography.titleMedium,
                color = if (ability.proficient) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp),
            )
            Text(
                text = ability.score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
            AbilityTileBottomRow(
                modifierValue = formatBonus(ability.modifier),
                savingThrowValue = formatBonus(ability.savingThrowBonus),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AbilityTileBottomRow(
    modifierValue: String,
    savingThrowValue: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AbilityTileStat(
            label = "Mod",
            value = modifierValue,
            modifier = Modifier.weight(1f),
        )
        AbilityTileStat(
            label = "Save",
            value = savingThrowValue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AbilityTileStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val RoundedOctagonShape: Shape = GenericShape { size, _ ->
    val inset = min(size.width, size.height) * 0.23f
    val radius = min(size.width, size.height) * 0.07f

    val points = listOf(
        Offset(inset, 0f),
        Offset(size.width - inset, 0f),
        Offset(size.width, inset),
        Offset(size.width, size.height - inset),
        Offset(size.width - inset, size.height),
        Offset(inset, size.height),
        Offset(0f, size.height - inset),
        Offset(0f, inset),
    )

    val rounded = points.mapIndexed { index, point ->
        val prev = points[(index - 1 + points.size) % points.size]
        val next = points[(index + 1) % points.size]
        val toPrev = prev - point
        val toNext = next - point
        val prevLength = hypot(toPrev.x.toDouble(), toPrev.y.toDouble()).toFloat().coerceAtLeast(0.001f)
        val nextLength = hypot(toNext.x.toDouble(), toNext.y.toDouble()).toFloat().coerceAtLeast(0.001f)
        val entry = point + (toPrev / prevLength) * radius
        val exit = point + (toNext / nextLength) * radius
        RoundedCorner(entry = entry, vertex = point, exit = exit)
    }

    moveTo(rounded.first().exit.x, rounded.first().exit.y)
    rounded.forEachIndexed { index, _ ->
        val next = rounded[(index + 1) % rounded.size]
        lineTo(next.entry.x, next.entry.y)
        quadraticTo(next.vertex.x, next.vertex.y, next.exit.x, next.exit.y)
    }
    close()
}

private data class RoundedCorner(
    val entry: Offset,
    val vertex: Offset,
    val exit: Offset,
)

private fun formatBonus(value: Int): String = if (value >= 0) "+$value" else value.toString()

@Preview(showBackground = true)
@Composable
private fun AbilityTokensGridPreview() {
    AppTheme {
        AbilityTokensGrid(
            abilities = listOf(
                AbilityTokenData("STR", 16, 3, 5, proficient = true),
                AbilityTokenData("DEX", 12, 1, 1),
                AbilityTokenData("CON", 14, 2, 4),
                AbilityTokenData("INT", 10, 0, 2),
                AbilityTokenData("WIS", 13, 1, 3, proficient = true),
                AbilityTokenData("CHA", 8, -1, 1),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
