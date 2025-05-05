package com.github.arhor.spellbindr.data.model

enum class DiceType(val sides: Int) {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    ;

    override fun toString(): String = "d$sides"
}