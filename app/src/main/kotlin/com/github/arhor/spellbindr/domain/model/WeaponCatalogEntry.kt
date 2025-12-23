package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.DamageType
import com.github.arhor.spellbindr.data.model.EquipmentCategory

/**
 * Represents a weapon entry in the catalog derived from equipment data.
 *
 * @property id Unique identifier for the weapon.
 * @property name Display name of the weapon.
 * @property categories Equipment categories associated with the weapon.
 * @property damageDiceCount Number of dice rolled for damage.
 * @property damageDieSize Size of each damage die.
 * @property damageType Type of damage dealt by the weapon.
 */
data class WeaponCatalogEntry(
    val id: String,
    val name: String,
    val categories: Set<EquipmentCategory>,
    val damageDiceCount: Int,
    val damageDieSize: Int,
    val damageType: DamageType,
)
