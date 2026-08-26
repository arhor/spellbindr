package io.github.arhor.dnd.companion.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
enum class CheckMode(val label: String) {
    NORMAL("Normal"),
    ADVANTAGE("Advantage"),
    DISADVANTAGE("Disadvantage"),
}
