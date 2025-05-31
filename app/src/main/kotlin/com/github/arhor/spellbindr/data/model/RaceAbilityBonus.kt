package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RaceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)
