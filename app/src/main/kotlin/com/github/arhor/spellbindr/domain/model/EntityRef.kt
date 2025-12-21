package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.utils.toTitleCase
import kotlinx.serialization.Serializable

/**
 * Represents a reference to another entity within the domain model.
 *
 * @property id The unique identifier of the referenced entity.
 */
@Serializable
data class EntityRef(
    val id: String,
) {
    fun prettyString(): String = id.toTitleCase('-')
}
