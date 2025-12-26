package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.Choice
import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val id: String,
    val name: String,
    val desc: List<String>,
    val choice: Choice? = null
)
