package com.github.arhor.spellbindr.core.common.data.model

@Suppress("unused")
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
            .joinToString(separator = "") { it.replaceFirstChar(Char::titlecaseChar) }
}
