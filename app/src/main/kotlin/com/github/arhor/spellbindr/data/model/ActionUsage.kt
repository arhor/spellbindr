package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActionUsage(
    val type: String,
    val dice: String? = null,
    @SerialName("min_value")
    val minValue: Int? = null
)
