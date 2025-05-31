package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbilitySpell(
    val name: String,
    val level: Int,
    val notes: String? = null,
    val usage: SpecialAbilityUsage? = null
)
