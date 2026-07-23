package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubclassSpell(
    val spell: String,
    val prerequisites: List<Prerequisite>? = null,
)
