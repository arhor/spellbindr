package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Race(
    val id: String,
    val name: String,
    val traits: List<String>? = null,
    val subraces: List<String>? = null,
)
