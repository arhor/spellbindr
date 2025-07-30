package com.github.arhor.spellbindr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun SpellbindrTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            onBackground = Accent,
        ),
        typography = SpellbindrTypography,
        content = content,
    )
}
