package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val name: String,
    val desc: String? = null,
    val type: String,
    val script: String? = null,
    val typicalSpeakers: List<String>,
)
