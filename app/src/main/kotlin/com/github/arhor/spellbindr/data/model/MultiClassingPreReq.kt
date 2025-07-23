package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MultiClassingPreReq(
    val abilityScore: EntityRef,
    val minimumScore: Int
)
