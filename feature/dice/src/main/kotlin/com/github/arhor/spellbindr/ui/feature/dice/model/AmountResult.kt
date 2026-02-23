package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class AmountResult(
    val groups: List<DiceGroupResult>,
    val total: Int,
)
