package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A typed ability-score requirement used by class multiclassing rules. */
@Serializable
data class AbilityScorePrerequisite(
    val abilityScore: List<AbilityId>,
    @SerialName("minimumValue")
    val minimumScore: Int,
    val atLeastOne: Boolean = false,
)

/** Kept as a source-compatible name for consumers of the original model. */
typealias MultiClassingPreReq = AbilityScorePrerequisite
