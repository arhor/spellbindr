package io.github.arhor.dnd.companion.domain.model

import io.github.arhor.dnd.companion.utils.toCapitalCase

enum class ThemeMode {
    LIGHT,
    DARK;

    val isDark: Boolean
        get() = this == DARK

    override fun toString(): String = name.toCapitalCase()
}
