package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the moral and ethical stance of a character or creature.
 *
 * @property id The unique identifier of the alignment.
 * @property name The full name of the alignment (e.g., "Lawful Good", "Chaotic Evil").
 * @property desc A detailed description of what this alignment entails.
 * @property abbr A common abbreviation for the alignment (e.g., "LG", "CE").
 */
@Serializable
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbr: String,
)
