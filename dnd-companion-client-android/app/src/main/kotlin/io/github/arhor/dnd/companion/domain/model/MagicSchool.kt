package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MagicSchool(
    val id: String,
    val name: String,
    val desc: List<String>,
)
