package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SpecialAbilitySpellcasting(
    val level: Int? = null,
    val ability: EntityRef,
    val dc: Int? = null,
    val modifier: Int? = null,
    val componentsRequired: List<String>,
    val school: String? = null,
    val slots: Map<String, Int>? = null,
    val spells: List<SpecialAbilitySpell>
)
