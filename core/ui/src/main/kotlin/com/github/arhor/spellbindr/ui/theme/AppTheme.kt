package com.github.arhor.spellbindr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    isDarkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isDarkTheme ?: isSystemInDarkTheme()) {
        AppDarkColorScheme
    } else {
        AppLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
