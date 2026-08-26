package io.github.arhor.dnd.companion.domain.model

import io.github.arhor.dnd.companion.domain.serialization.EntityRefSerializer
import io.github.arhor.dnd.companion.utils.toTitleCase
import kotlinx.serialization.Serializable

/**
 * Represents a reference to another entity within the data model.
 * This is typically used to link to other data objects like abilities, skills, equipment, etc.
 *
 * @property id The unique identifier of the referenced entity.
 */
@JvmInline
@Serializable(with = EntityRefSerializer::class)
value class EntityRef(
    val id: String,
) {
    fun prettyString(): String = id.toTitleCase('-')
}
