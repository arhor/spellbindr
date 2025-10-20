package com.github.arhor.spellbindr.ui.feature.diceRoller.model

import kotlinx.serialization.Serializable

/**
 * Represents a single dice roll result
 */
@Serializable
data class DiceRoll(
    val diceType: DiceType,
    val result: Int,
) {
    init {
        require(result in 1..diceType.sides) { "Dice result must be between 1 and ${diceType.sides}" }
    }

    val isCriticalHit: Boolean
        get() = diceType == DiceType.D20 && result == 20

    val isCriticalMiss: Boolean
        get() = diceType == DiceType.D20 && result == 1
}
