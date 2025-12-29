package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class MultiClassingPreReq(
    val abilityScore: EntityRef,
    val minimumScore: Int
)
