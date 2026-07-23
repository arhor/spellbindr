package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Usage(
    val type: String,
    val times: Int
)
