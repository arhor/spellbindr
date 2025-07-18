package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class Proficiency(
    val id: String,
    val name: String
)
