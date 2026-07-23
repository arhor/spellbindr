package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import java.util.Locale

internal fun Int.toOrdinalLabel(): String {
    val absValue = this.coerceAtLeast(0)
    val mod100 = absValue % 100
    val suffix = when {
        mod100 in 11..13 -> "th"
        absValue % 10 == 1 -> "st"
        absValue % 10 == 2 -> "nd"
        absValue % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$absValue$suffix".lowercase(Locale.ROOT)
}

