package io.github.arhor.dnd.companion.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class DiceGroup(
    val sides: Int,
    val count: Int,
)
