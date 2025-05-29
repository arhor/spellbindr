package com.github.arhor.spellbindr.data.next.model

import kotlinx.serialization.Serializable

@Serializable
data class CountedReferencePrerequisite(
    val type: String,
    val proficiency: EntityRef? = null
)
