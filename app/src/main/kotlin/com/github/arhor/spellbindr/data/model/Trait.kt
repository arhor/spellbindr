package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val desc: List<String>,

    val proficiencies: List<Proficiency>,
    val proficiencyChoices: Choice? = null,
    val languageOptions: Choice? = null,
    val parent: EntityRef? = null,
    val traitSpecific: TraitSpecific? = null,

    val effects: List<Effect> = emptyList()
)
