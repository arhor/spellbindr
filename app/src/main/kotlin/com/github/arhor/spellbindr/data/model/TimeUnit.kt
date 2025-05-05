package com.github.arhor.spellbindr.data.model

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

enum class TimeUnit {
    ACTION,
    BONUS_ACTION,
    REACTION,
    MINUTE,
    HOUR,
    ;

    override fun toString(): String =
        super.toString()
            .lowercase()
            .split(' ')
            .joinToString(separator = "") { it.capitalize(Locale.current) }
}