package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


/**
 * Represents a prerequisite that requires a certain number of proficiencies of a specific type.
 *
 * @property type The type of proficiency required (e.g., "skill", "weapon", "armor").
 * @property proficiency Optional reference to a specific proficiency. If null, any proficiency of the specified type counts.
 */
@Serializable
data class CountedReferencePrerequisite(
    val type: String,
    val proficiency: DomainEntityRef? = null,
)
