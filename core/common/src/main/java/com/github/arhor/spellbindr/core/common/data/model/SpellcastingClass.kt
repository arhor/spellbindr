package com.github.arhor.spellbindr.core.common.data.model

enum class SpellcastingClass {
    BARD,
    CLERIC,
    DRUID,
    PALADIN,
    RANGER,
    SORCERER,
    WARLOCK,
    WIZARD,
    ARTIFICER,
    RITUAL_CASTER,
    ;

    override fun toString(): String =
        super.toString()
            .lowercase()
            .split('_')
            .joinToString(separator = " ") { it.replaceFirstChar(Char::titlecaseChar) }
}
