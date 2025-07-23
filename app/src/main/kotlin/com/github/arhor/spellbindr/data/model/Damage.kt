package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents damage information, including the damage dice and type.
 *
 * @property damageDice A string representing the damage dice (e.g., "1d6", "2d8+3").
 * @property damageType An [EntityRef] referencing the type of damage (e.g., fire, cold, slashing).
 */
@Serializable
data class Damage(
    val damageDice: String,
    val damageType: EntityRef
)
