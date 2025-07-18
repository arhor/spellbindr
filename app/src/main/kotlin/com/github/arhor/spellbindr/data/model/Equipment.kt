package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    val id: String,
    val name: String,
    val desc: List<String>? = null,
    val armorClass: EquipmentArmorClass? = null,
    val capacity: String? = null,
    val contents: List<Content>? = null,
    val cost: Cost,
    val damage: Damage? = null,
    val properties: List<EntityRef>? = null,
    val quantity: Int? = null,
    val range: Range? = null,
    val special: List<String>? = null,
    val speed: EquipmentSpeed? = null,
    val stealthDisadvantage: Boolean? = null,
    val strMinimum: Int? = null,
    val throwRange: Range? = null,
    val twoHandedDamage: Damage? = null,
    val weight: Double? = null,
    val categories: Set<EquipmentCategory>,
)
