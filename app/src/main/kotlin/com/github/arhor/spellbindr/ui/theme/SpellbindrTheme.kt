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
            primary = Purple40,
            secondary = PurpleGrey40,
            tertiary = Pink40,
            onBackground = Accent,
            surfaceContainer = Purple40,
        ),
        typography = SpellbindrTypography,
        content = content,
    )
}
