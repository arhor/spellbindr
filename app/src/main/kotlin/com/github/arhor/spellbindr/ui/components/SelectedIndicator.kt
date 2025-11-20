package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SelectedIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val canvasWidth = 20.dp
    val strokeWidth = 2.dp
    val circleColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Canvas(
        modifier = modifier
            .wrapContentSize(Alignment.Center)
            .padding(strokeWidth)
            .requiredSize(canvasWidth)
    ) {
        val strokeWidthAsPx = strokeWidth.toPx()
        val strokeWidthHalf = strokeWidthAsPx / 2
        val primaryRadiusPx = canvasWidth.toPx() / 2 - strokeWidthHalf

        if (selected) {
            drawCircle(
                color = circleColor,
                radius = primaryRadiusPx,
                style = Stroke(width = strokeWidthAsPx),
            )
            drawCircle(
                color = circleColor,
                radius = 6.dp.toPx() - strokeWidthHalf,
                style = Fill
            )
        } else {
            drawCircle(
                color = circleColor,
                radius = primaryRadiusPx,
                style = Stroke(
                    width = strokeWidthAsPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 9f)),
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedIndicatorLightPreview() {
    AppTheme(isDarkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SelectedIndicator(selected = true)
                SelectedIndicator(selected = false)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedIndicatorDarkPreview() {
    AppTheme(isDarkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SelectedIndicator(selected = true)
                SelectedIndicator(selected = false)
            }
        }
    }
}
