package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Subrace(
    val name: String,
    val desc: String,
    val traits: List<String>,
)
