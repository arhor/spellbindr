package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentRef(
    val equipment: EntityRef,
    val quantity: Int
)
