package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.DifficultyClass
import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    val name: String,
    val desc: String,
    val dc: DifficultyClass? = null
)
