package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the different types of dice used in D&D
 */
@Serializable
enum class DiceType(val sides: Int, val displayName: String) {
    D4(4, "d4"),
    D6(6, "d6"),
    D8(8, "d8"),
    D10(10, "d10"),
    D12(12, "d12"),
    D20(20, "d20"),
    D100(100, "d100")
}

/**
 * Represents a single dice roll result
 */
@Serializable
data class DiceRoll(
    val diceType: DiceType,
    val result: Int,
    val isCriticalHit: Boolean = false, // Natural 20 on d20
    val isCriticalMiss: Boolean = false // Natural 1 on d20
) {
    init {
        require(result in 1..diceType.sides) { "Dice result must be between 1 and ${diceType.sides}" }
    }
}

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

/**
 * Represents a roll history entry with unique ID
 */
@Serializable
data class RollHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val rollSet: RollSet,
    val timestamp: Long = System.currentTimeMillis()
)
