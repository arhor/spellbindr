package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class DiceGroupResult(
    val sides: Int,
    val rolls: List<Int>,
) {
    val count: Int get() = rolls.size
    val subtotal: Int get() = rolls.sum()
}
