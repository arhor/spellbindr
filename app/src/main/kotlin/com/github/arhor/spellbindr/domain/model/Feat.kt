package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Feat(
    val id: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<Prerequisite> = emptyList(),
    val effects: List<Effect>? = null,
    val abilityBonusChoice: Choice? = null,
    val spellChoice: Choice? = null,
    val languageChoice: Choice? = null,
    val proficiencyChoice: Choice? = null,
)
