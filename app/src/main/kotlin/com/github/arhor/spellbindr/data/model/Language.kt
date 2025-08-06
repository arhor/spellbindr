package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val name: String,
    val desc: String?,
    val type: String,
    val script: String?,
    val typicalSpeakers: List<String>,
)
