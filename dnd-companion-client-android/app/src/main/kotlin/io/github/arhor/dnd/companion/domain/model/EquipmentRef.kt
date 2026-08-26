package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRef(
    val id: String,
    val quantity: Int,
)
