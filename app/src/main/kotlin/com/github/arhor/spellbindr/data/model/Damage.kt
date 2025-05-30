package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Damage(
    val damageDice: String,
    val damageType: EntityRef
)
