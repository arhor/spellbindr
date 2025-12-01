package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .drawWithCache {
                val horizontalGradientBrush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0f),
                        color.copy(alpha = 0.6f),
                        color,
                        color.copy(alpha = 0.6f),
                        color.copy(alpha = 0f)
                    )
                )
                onDrawBehind {
                    drawRect(
                        size = size,
                        brush = horizontalGradientBrush,
                    )
                }
            }
    )
}

@Preview
@Composable
private fun GradientDividerLightPreview() {
    GradientDividerPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun GradientDividerDarkPreview() {
    GradientDividerPreview(isDarkTheme = true)
}

@Composable
private fun GradientDividerPreview(isDarkTheme: Boolean) {
    AppTheme(isDarkTheme = isDarkTheme) {
        GradientDivider(modifier = Modifier.height(2.dp))
    }
}
