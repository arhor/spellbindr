package com.github.arhor.spellbindr.data.common

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM;

    override fun toString(): String =
        name.lowercase().replaceFirstChar { it.titlecase() }
} 
