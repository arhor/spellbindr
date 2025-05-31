package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentSpeed(
    val quantity: Double,
    val unit: String
)
