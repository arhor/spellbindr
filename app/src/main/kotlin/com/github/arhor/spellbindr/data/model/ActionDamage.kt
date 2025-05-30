package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ActionDamage(
    val damageType: EntityRef,
    val damageAtCharacterLevel: Map<String, String>
)
