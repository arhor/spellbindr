package com.github.arhor.spellbindr.data.model

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM;

    override fun toString(): String =
        name.lowercase().replaceFirstChar { it.titlecase() }
} 
