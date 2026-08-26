package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class DiceGroup(
    val sides: Int,
    val count: Int,
)
