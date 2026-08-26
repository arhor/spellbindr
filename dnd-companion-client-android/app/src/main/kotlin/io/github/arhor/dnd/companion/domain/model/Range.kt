package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val base: Int,
    val long: Int? = null,
)
