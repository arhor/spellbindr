package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbilityUsage(
    val type: String,
    val times: Int? = null,
    val restTypes: List<String>? = null
)
