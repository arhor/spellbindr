package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Race(
    val id: String,
    val name: String,
    val age: String,
    val alignment: String,
    val size: String,
    val sizeDescription: String,
    val speed: Int,
    val abilityBonuses: List<RaceAbilityBonus>,
    val abilityBonusOptions: Choice? = null,
    val languageDesc: String,
    val languageOptions: Choice,
    val languages: List<EntityRef>,
    val startingProficiencies: List<EntityRef>? = null,
    val startingProficiencyOptions: Choice? = null,
    val subraces: List<EntityRef>? = null,
    val traits: List<EntityRef>? = null
)
