package com.github.arhor.spellbindr.ui.feature.characters

/**
 * Represents a spell selected for association with a character.
 *
 * @param spellId Canonical spell identifier.
 * @param sourceClass The spellcasting class that granted the spell, used for grouping.
 */
data class CharacterSpellAssignment(
    val spellId: String,
    val sourceClass: String = "",
)
