package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentArmorClass(
    val base: Int,
    @SerialName("dex_bonus")
    val dexBonus: Boolean,
    @SerialName("max_bonus")
    val maxBonus: Int? = null
)

