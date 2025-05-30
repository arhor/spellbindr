package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Subrace(
    val id: String,
    val name: String,
    val desc: String,
    val race: EntityRef,
    val abilityBonuses: List<SubraceAbilityBonus>,
    val languages: List<EntityRef>? = null,
    val languageOptions: Choice? = null,
    val racialTraits: List<EntityRef>,
    val startingProficiencies: List<EntityRef>? = null
)
