package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.utils.toCapitalCase

enum class ThemeMode {
    LIGHT,
    DARK;

    val isDark: Boolean
        get() = this == DARK

    override fun toString(): String = name.toCapitalCase()
}
