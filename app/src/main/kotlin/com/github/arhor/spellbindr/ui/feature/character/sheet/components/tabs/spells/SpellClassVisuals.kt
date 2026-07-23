package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

internal fun classIconFor(sourceKey: String): ImageVector = when (sourceKey.lowercase()) {
    "wizard" -> Icons.Outlined.AutoFixHigh
    "paladin" -> Icons.Outlined.Security
    "warlock" -> Icons.Outlined.DarkMode
    else -> Icons.Outlined.Star
}

@Composable
internal fun classAccentColorFor(sourceKey: String): Color {
    return when (sourceKey.lowercase()) {
        "wizard" -> MaterialTheme.colorScheme.secondary
        "paladin" -> MaterialTheme.colorScheme.primary
        "warlock" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}
