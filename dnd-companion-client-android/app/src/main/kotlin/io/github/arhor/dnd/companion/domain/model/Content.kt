package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class Content(
    val item: EntityRef,
    val quantity: Int,
)
