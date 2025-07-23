package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.Accent

@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = Accent,
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
