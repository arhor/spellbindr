package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    val name: String,
    val desc: String,
    val dc: DifficultyClass? = null
)
