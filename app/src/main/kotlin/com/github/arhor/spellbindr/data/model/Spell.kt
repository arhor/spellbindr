package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val name: String,
    val desc: String,
    val level: Int,
    val range: Range,
    val school: MagicSchool,
    val castingTime: String,
    val components: Set<Component>,
    val duration: String,
    val higherLevel: String? = null,
    val classes: List<SpellcastingClass>,
    val ritual: Boolean = false,
    val concentration: Boolean = false,
    val damage: Damage? = null,
    val source: Source,
)
