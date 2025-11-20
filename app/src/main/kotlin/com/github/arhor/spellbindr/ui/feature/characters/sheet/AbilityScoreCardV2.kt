package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.ui.theme.RoundedHexShape
import com.github.arhor.spellbindr.utils.signed

data class AbilityScore(
    val name: String,
    val value: Int,
    val bonus: Int,
)

@Composable
fun AbilityScoreCardV2(
    ability: AbilityScore,
    modifier: Modifier = Modifier,
    cardSize: Dp = 72.dp,
) {
    val bonusText = signed(ability.bonus)

    Box(
        modifier = modifier
            .width(cardSize)
            .height(cardSize),
        contentAlignment = Alignment.TopCenter,
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(cardSize)
                .height(cardSize),
            shape = RoundedHexShape(0.05f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = ability.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = ability.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
        AbilityBonusBadge(
            bonusText = bonusText,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AbilityBonusBadge(
    bonusText: String,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large

    Surface(
        modifier = modifier.drawWithContent {
            // Draw surface (background + built-in shadow) and text first
            drawContent()

            val strokeWidth = 1.dp.toPx()
            val path = createPath(shape)

            // --- TOP HALF: highlight (stronger at top-left) ---
            clipRect(
                left = 0f,
                top = -size.height,
                right = size.width,
                bottom = size.height / 2f + strokeWidth, // include midline
            ) {
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f), // top-left brightest
                            Color.White.copy(alpha = 0.3f), // fades toward top-right
                        ),
                        startX = 0f,
                        endX = size.width,
                    ),
                    style = Stroke(width = strokeWidth),
                )
            }

            // --- BOTTOM HALF: shadow (stronger at bottom-right) ---
            clipRect(
                left = -size.width,
                top = size.height / 2f - strokeWidth,
                right = size.width + strokeWidth,
                bottom = size.height + strokeWidth,
            ) {
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.05f), // very soft bottom-left
                            Color.Black.copy(alpha = 0.25f), // stronger at bottom-right
                            Color.Black.copy(alpha = 0.10f), // make it softer to the line end
                        ),
                        startX = 0f,
                        endX = size.width,
                    ),
                    style = Stroke(width = strokeWidth),
                )
            }
        },
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 5.dp,
        shadowElevation = 3.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = bonusText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

private fun ContentDrawScope.createPath(shape: CornerBasedShape): Path =
    when (val outline = shape.createOutline(size, layoutDirection, this)) {
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Generic -> outline.path
    }


@Preview(showBackground = true)
@Composable
private fun AbilityScoreCardV2Preview() {
    AppTheme(isDarkTheme = true) {
        Surface(
            tonalElevation = 1.dp,
        ) {
            AbilityScoreCardV2(
                ability = AbilityScore(
                    name = "CON",
                    value = 13,
                    bonus = 1,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
