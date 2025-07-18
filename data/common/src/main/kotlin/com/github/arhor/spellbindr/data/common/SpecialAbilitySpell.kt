package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbilitySpell(
    val name: String,
    val level: Int,
    val notes: String? = null,
    val usage: SpecialAbilityUsage? = null
)
