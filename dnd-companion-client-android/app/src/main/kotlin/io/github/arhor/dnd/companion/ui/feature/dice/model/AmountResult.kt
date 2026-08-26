package io.github.arhor.dnd.companion.ui.feature.dice.model

import androidx.compose.runtime.Immutable

@Immutable
data class AmountResult(
    val groups: List<DiceGroupResult>,
    val total: Int,
)
