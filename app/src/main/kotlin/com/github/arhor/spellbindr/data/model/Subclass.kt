package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Subclass(
    val id: String,
    val name: String,
    val desc: List<String>,
    val spells: List<SubclassSpell>? = null,
    val subclassFlavor: String,
)
