package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagicItem(
    val id: String,
    val name: String,
    val desc: List<String>,
    val image: String? = null,
    val rarity: Rarity,
    val variant: Boolean,
    val variants: List<EntityRef>,
    @SerialName("equipment_category")
    val equipmentCategory: EntityRef
)
