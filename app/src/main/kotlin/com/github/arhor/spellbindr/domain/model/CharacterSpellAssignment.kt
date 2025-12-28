package com.github.arhor.spellbindr.domain.model

import androidx.compose.runtime.Immutable
import java.io.Serializable

/**
 * Represents a spell selected for association with a character.
 *
 * @param spellId Canonical spell identifier.
 * @param sourceClass The spellcasting class that granted the spell, used for grouping.
 */
@Immutable
data class CharacterSpellAssignment(
    val spellId: String,
    val sourceClass: String = "",
) : Serializable
