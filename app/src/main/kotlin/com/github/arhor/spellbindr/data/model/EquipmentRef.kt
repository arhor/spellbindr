package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRef(
    val equipment: EntityRef,
    val quantity: Int
)
