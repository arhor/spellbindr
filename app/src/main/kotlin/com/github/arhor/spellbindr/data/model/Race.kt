package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Race(
    @SerialName("ability_bonus_options")
    val abilityBonusOptions: Choice? = null,
    val abilityBonuses: List<RaceAbilityBonus>,
    val age: String,
    val alignment: String,
    val id: String,
    @SerialName("language_desc")
    val languageDesc: String,
    val languageOptions: Choice,
    val languages: List<EntityRef>,
    val name: String,
    val size: String,
    @SerialName("size_description")
    val sizeDescription: String,
    val speed: Int,
    val startingProficiencies: List<EntityRef>? = null,
    @SerialName("starting_proficiency_options")
    val startingProficiencyOptions: Choice? = null,
    val subraces: List<EntityRef>? = null,
    val traits: List<EntityRef>? = null
)
