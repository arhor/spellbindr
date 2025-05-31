package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TraitSpecific(
    val subtraitOptions: Choice? = null,
    val spellOptions: Choice? = null,
    val damageType: EntityRef? = null,
    val breathWeapon: BreathWeaponAction? = null
)
