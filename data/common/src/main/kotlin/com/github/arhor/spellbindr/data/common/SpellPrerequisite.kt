package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class SpellPrerequisite(
    val type: String,
    val spell: String
)
