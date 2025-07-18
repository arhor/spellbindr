package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

/**
 * Represents an ability score in D&D, such as Strength, Dexterity, or Constitution.
 *
 * @property id A unique identifier for the ability score.
 * @property name The short name of the ability score (e.g., "STR").
 * @property desc A list of strings describing the ability score.
 * @property fullName The full name of the ability score (e.g., "Strength").
 * @property skills A list of skills associated with this ability score. Each skill is represented by an [com.github.arhor.spellbindr.data.common.EntityRef].
 */
@Serializable
data class AbilityScore(
    val id: String,
    val name: String,
    val desc: List<String>,
    val fullName: String,
    val skills: List<EntityRef>
)
