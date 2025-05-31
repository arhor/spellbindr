package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GenericInfo(
    val name: String,
    val desc: List<String>
)
