package com.github.arhor.spellbindr.data.model.next

/**
 * Represents a reference to another entity within the data model.
 * This is typically used to link to other data objects like abilities, skills, equipment, etc.
 *
 * @property id The unique identifier of the referenced entity.
 */
@JvmInline
value class Reference(val id: String)
