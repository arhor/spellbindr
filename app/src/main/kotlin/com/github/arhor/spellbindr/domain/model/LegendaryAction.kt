package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LegendaryAction(
    val name: String,
    val desc: String,
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null
)
