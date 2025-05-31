package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subclass(
    val id: String,
    val name: String,
    val desc: List<String>,
    @SerialName("class")
    val clazz: EntityRef,
    val spells: List<SubclassSpell>? = null,
    val subclassFlavor: String,
    val subclassLevels: String
)
