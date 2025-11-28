package com.github.arhor.spellbindr.data.model.next

import com.github.arhor.spellbindr.utils.ReferenceSerializer
import kotlinx.serialization.Serializable

/**
 * Represents a reference to another entity within the data model.
 * This is typically used to link to other data objects like abilities, skills, equipment, etc.
 *
 * @property id The unique identifier of the referenced entity.
 */
@JvmInline
@Serializable(with = ReferenceSerializer::class)
value class Reference(val id: String)
