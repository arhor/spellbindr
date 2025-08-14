package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubclassSpell(
    val spell: EntityRef,
    val prerequisites: List<Prerequisite>? = null,
)
