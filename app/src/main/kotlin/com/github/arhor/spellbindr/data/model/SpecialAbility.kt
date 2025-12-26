package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.DifficultyClass
import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbility(
    val name: String,
    val desc: String,
    val attackBonus: Int? = null,
    val damage: List<Damage>? = null,
    val dc: DifficultyClass? = null,
    val spellcasting: SpecialAbilitySpellcasting? = null,
    val usage: SpecialAbilityUsage
)
