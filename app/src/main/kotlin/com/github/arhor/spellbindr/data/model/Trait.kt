package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val desc: List<String>,
    val effects: List<Effect>? = null,

    val proficienciesChoice: Choice? = null,

    val languageOptions: Choice? = null,
    val parent: EntityRef? = null,
    val traitSpecific: TraitSpecific? = null,
)


