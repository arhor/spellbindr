package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Proficiency(
    val id: String,
    val name: String,
    val type: String,
    val reference: String,
)
