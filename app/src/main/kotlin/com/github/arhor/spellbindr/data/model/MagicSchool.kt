package com.github.arhor.spellbindr.data.model

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

enum class MagicSchool {
    ABJURATION,
    CONJURATION,
    DIVINATION,
    ENCHANTMENT,
    EVOCATION,
    ILLUSION,
    NECROMANCY,
    TRANSMUTATION,
    ;

    override fun toString(): String = super.toString().lowercase().capitalize(Locale.current)
}
