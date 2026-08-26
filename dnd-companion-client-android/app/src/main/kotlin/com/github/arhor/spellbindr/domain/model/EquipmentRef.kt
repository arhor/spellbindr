package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRef(
    val id: String,
    val quantity: Int,
)
