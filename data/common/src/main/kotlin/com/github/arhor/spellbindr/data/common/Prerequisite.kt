package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class Prerequisite(
    val id: String,
    val name: String,
    val type: String
)
