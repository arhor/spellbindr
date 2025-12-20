package com.github.arhor.spellbindr.domain.model

enum class ThemeMode {
    LIGHT,
    DARK;

    val isDark: Boolean
        get() = this == DARK

    override fun toString(): String =
        name.lowercase().replaceFirstChar { it.titlecase() }

    companion object {
        fun fromIsDark(isDark: Boolean): ThemeMode =
            if (isDark) DARK else LIGHT
    }
}
