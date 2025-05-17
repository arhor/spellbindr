package com.github.arhor.spellbindr.core.common.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RaceList(
    val data: List<Race>
)

@Serializable
data class Race(
    val name: String,
    val source: String,
    val traits: List<Trait>,
    val subraces: List<Subrace> = emptyList()
)

@Serializable
data class Effect(
    val abilities: List<Map<String, Int>>? = null,
    val size: String? = null,
    val speed: Int? = null,
    val skills: List<String>? = null,
    val languages: List<String>? = null
)

@Serializable
data class Trait(
    val name: String,
    val desc: String,
    val table: Map<String, List<String>>? = null,
    val effect: Effect? = null
)

@Serializable
data class Subrace(
    val name: String,
    val source: String,
    val traits: List<Trait>
)
