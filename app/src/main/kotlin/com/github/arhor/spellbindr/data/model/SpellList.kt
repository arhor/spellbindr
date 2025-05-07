package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SpellList(
    val name: String,
    val spellNames: List<String>
) 