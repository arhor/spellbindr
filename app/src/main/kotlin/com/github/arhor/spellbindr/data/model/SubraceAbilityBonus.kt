package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubraceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)
