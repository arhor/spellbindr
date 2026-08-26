package io.github.arhor.dnd.companion.ui.feature.dice.components

import io.github.arhor.dnd.companion.ui.feature.dice.model.CheckMode
import io.github.arhor.dnd.companion.ui.feature.dice.model.DiceGroupResult
import kotlin.math.abs

internal fun formatSignedValue(value: Int, showPlusForZero: Boolean = true): String {
    return when {
        value > 0 -> "+$value"
        value < 0 -> "-${abs(value)}"
        showPlusForZero -> "+0"
        else -> "0"
    }
}

internal fun CheckMode.shortLabel(): String = when (this) {
    CheckMode.NORMAL -> "Normal"
    CheckMode.ADVANTAGE -> "Advantage"
    CheckMode.DISADVANTAGE -> "Disadvantage"
}

internal fun DiceGroupResult.summary(): String {
    val rollValues = rolls.joinToString(", ")
    return "${count}d$sides ($rollValues)"
}
