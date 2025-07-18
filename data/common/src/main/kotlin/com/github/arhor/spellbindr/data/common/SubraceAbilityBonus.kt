package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class SubraceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)
