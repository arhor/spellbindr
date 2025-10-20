package com.github.arhor.spellbindr.ui.feature.diceRoller.model

import kotlinx.serialization.Serializable

/**
 * Represents a complete set of dice rolls (e.g., "3d6 + 2")
 */
@Serializable
data class RollSet(
    val diceType: DiceType,
    val quantity: Int,
    val rolls: List<DiceRoll> = emptyList(),
    val total: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayText: String
        get() = "${quantity}${diceType.displayName} = $total"

    val individualResults: String
        get() = rolls.joinToString(", ", "[", "]") { it.result.toString() }
}
