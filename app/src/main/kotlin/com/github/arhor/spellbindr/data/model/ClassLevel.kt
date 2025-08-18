package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ClassLevel(
    val id: String,
    val level: Int,
    val features: List<String>,
)
