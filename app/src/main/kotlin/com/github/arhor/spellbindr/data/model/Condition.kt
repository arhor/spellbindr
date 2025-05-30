package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Condition(
    val id: String,
    val name: String,
    val desc: List<String>
)
