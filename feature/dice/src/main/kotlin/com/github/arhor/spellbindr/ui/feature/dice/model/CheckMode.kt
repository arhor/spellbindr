package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CheckMode(val label: String) {
    NORMAL("Normal"),
    ADVANTAGE("Advantage"),
    DISADVANTAGE("Disadvantage"),
}
