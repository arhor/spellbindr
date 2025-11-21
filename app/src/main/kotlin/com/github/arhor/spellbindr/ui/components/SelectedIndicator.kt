package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SelectedIndicator(
    modifier: Modifier = Modifier,
    selected: Boolean,
) {
    val canvasWidth = 16.dp
    val strokeWidth = 1.5.dp
    val circleColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .padding(strokeWidth)
            .requiredSize(canvasWidth)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
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
                radius = 5.dp.toPx() - strokeWidthHalf,
                style = Fill
            )
        } else {
            drawCircle(
                color = circleColor,
                radius = primaryRadiusPx,
                style = Stroke(
                    width = strokeWidthAsPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f)),
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedIndicatorLightPreview() {
    AppTheme(isDarkTheme = false) {
        SelectedIndicatorPreview()
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedIndicatorDarkPreview() {
    AppTheme(isDarkTheme = true) {
        SelectedIndicatorPreview()
    }
}

@Composable
private fun SelectedIndicatorPreview() {
    Surface {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SelectedIndicator(selected = true)
            SelectedIndicator(selected = false)
        }
    }
}
