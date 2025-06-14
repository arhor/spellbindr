package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Prerequisite(
    val id: String,
    val name: String,
    val type: String
)
