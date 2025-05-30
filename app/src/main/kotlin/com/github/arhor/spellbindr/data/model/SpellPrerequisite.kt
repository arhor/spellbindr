package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SpellPrerequisite(
    val type: String,
    val spell: String
)
