package com.github.arhor.spellbindr.data.model

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

    override fun toString(): String = name.lowercase().replaceFirstChar(Char::titlecaseChar)
}
