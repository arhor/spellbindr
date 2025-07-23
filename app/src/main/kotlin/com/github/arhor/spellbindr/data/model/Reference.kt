package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reference(
    val id: String,
    val name: String,
    val type: String
)
