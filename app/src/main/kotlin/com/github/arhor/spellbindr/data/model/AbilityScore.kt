package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AbilityScore(
    val id: String,
    val name: String,
    val desc: List<String>,
    val fullName: String,
    val skills: List<EntityRef>
)
