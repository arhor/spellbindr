package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GenericInfo(
    val name: String,
    val desc: List<String>
)
