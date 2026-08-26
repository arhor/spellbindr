package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClassLevel(
    val id: String,
    val level: Int,
    val features: List<String>,
    val spellcasting: LevelSpellcasting? = null,
)

@Serializable
data class LevelSpellcasting(
    val cantrips: Int? = null,
    val spells: Int? = null,
    val spellSlots: Map<String, Int>? = null,
)
