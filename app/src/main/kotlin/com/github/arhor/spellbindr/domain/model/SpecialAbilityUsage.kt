package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbilityUsage(
    val type: String,
    val times: Int? = null,
    @SerialName("rest_types")
    val restTypes: List<String>? = null
)
