package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class SubclassSpell(
    val spell: EntityRef,
    val prerequisites: List<Prerequisite>
)
