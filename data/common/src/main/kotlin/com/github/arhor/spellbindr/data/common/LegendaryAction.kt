package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class LegendaryAction(
    val name: String,
    val desc: String,
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null
)
