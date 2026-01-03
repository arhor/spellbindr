package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


/**
 * Represents the Difficulty Class (DC) for a spell.
 *
 * @property desc A description of the spell's DC, if any.
 * @property dcType A reference to the type of DC (e.g., Dexterity saving throw).
 * @property dcSuccess Describes what happens on a successful save.
 */
@Serializable
data class SpellDC(
    val desc: String? = null,
    val dcType: EntityRef,
    val dcSuccess: String,
)
