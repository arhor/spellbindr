package com.github.arhor.spellbindr.ui.feature.diceRoller.model

import kotlinx.serialization.Serializable

/**
 * Represents the different types of dice used in D&D
 */
@Serializable
enum class DiceType(val sides: Int) {
    D4(sides = 4),
    D6(sides = 6),
    D8(sides = 8),
    D10(sides = 10),
    D12(sides = 12),
    D20(sides = 20),
    D100(sides = 100),
    ;

    val displayName: String
        get() = "d$sides"
}
