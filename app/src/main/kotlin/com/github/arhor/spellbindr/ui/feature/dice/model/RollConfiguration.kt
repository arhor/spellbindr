package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class RollConfiguration(
    val hasCheck: Boolean,
    val checkMode: CheckMode,
    val checkModifier: Int,
    val amountDice: List<DiceGroup>,
)
