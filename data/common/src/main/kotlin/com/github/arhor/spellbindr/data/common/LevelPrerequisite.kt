package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class LevelPrerequisite(
    val type: String,
    val level: Int
)
