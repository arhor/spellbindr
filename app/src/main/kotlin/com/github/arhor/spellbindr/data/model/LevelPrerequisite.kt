package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LevelPrerequisite(
    val type: String,
    val level: Int
)
