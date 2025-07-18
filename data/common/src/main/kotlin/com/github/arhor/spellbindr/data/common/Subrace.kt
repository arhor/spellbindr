package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a subrace with a name, description, and a list of traits.
 *
 * @property id The unique identifier for the subrace.
 * @property name The name of the subrace.
 * @property description A description of the subrace.
 * @property traits A list of trait IDs associated with the subrace.
 */
@Serializable
data class Subrace(
    val id: String,
    val name: String,
    @SerialName("desc")
    val description: String,
    val traits: List<String>,
)
