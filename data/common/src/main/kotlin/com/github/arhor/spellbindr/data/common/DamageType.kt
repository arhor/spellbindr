package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

/**
 * Represents a type of damage.
 *
 * @property id The unique identifier of the damage type.
 * @property name The name of the damage type.
 * @property desc A list of strings describing the damage type.
 */
@Serializable
data class DamageType(
    val id: String,
    val name: String,
    val desc: List<String>,
)
