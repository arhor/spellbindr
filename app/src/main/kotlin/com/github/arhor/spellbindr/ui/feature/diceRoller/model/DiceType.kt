package com.github.arhor.spellbindr.ui.feature.diceRoller.model

import kotlinx.serialization.Serializable

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
