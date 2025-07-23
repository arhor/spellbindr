package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val type: String,
    val times: Int
)
