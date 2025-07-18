package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.Serializable

@Serializable
data class RaceAbilityBonus(
    val abilityScore: EntityRef,
    val bonus: Int
)
