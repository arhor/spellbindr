package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.util.toTitleCase
import kotlinx.serialization.Serializable

/**
 * Represents a reference to another entity within the data model.
 * This is typically used to link to other data objects like abilities, skills, equipment, etc.
 *
 * @property id The unique identifier of the referenced entity.
 */
@Serializable
data class EntityRef(
    val id: String,
) {
    fun prettyString(): String = id.toTitleCase('-')
}
