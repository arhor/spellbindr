package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a subrace with a name, description, and a list of traits.
 *
 * @property name The name of the subrace.
 * @property desc A description of the subrace.
 * @property traits A list of trait IDs associated with the subrace.
 */
@Serializable
data class Subrace(
    val name: String,
    val desc: String,
    val traits: List<String>,
)
