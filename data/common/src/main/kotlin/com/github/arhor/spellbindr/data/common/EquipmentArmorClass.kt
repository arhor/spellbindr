package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

/**
 * Represents the armor class provided by an equipment item.
 *
 * @property base The base armor class value.
 * @property dexBonus Whether the Dexterity modifier is applied to the armor class.
 * @property maxBonus The maximum Dexterity bonus that can be applied, if any.
 */
@Serializable
data class EquipmentArmorClass(
    val base: Int,
    val dexBonus: Boolean,
    val maxBonus: Int? = null
)

