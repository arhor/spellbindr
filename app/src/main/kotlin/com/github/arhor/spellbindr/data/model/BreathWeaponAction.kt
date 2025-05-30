package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BreathWeaponAction(
    val name: String,
    val desc: String,
    val usage: Usage,
    val dc: DifficultyClass,
    val damage: List<ActionDamage>,
    val areaOfEffect: AreaOfEffect,
)
