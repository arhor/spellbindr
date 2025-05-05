package com.github.arhor.spellbindr.data.model

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

enum class DamageType {
    ACID,
    BLUDGEONING,
    COLD,
    FIRE,
    FORCE,
    LIGHTNING,
    NECROTIC,
    PIERCING,
    POISON,
    PSYCHIC,
    RADIANT,
    SLASHING,
    THUNDER,
    NONE,
    ;

    override fun toString(): String = super.toString().lowercase().capitalize(Locale.current)
}
