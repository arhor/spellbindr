package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbr: String,
)
