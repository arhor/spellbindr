package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


@Serializable
data class SubraceAbilityBonus(
    val abilityScore: DomainEntityRef,
    val bonus: Int
)
