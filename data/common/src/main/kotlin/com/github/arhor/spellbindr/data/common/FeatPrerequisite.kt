package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class FeatPrerequisite(
    val abilityScore: EntityRef,
    val minimumScore: Int
)
