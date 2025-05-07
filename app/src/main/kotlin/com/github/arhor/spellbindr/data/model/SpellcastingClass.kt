package com.github.arhor.spellbindr.data.model

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

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
            .joinToString(separator = " ") { it.capitalize(Locale.current) }
}