package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentSpeed(
    val quantity: Double,
    val unit: String
)
