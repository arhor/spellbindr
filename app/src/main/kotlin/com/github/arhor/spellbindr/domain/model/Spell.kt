package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.AreaOfEffect
import com.github.arhor.spellbindr.data.model.SpellDamage
import com.github.arhor.spellbindr.data.model.SpellDC

data class Spell(
    val id: String,
    val name: String,
    val desc: List<String>,
    val level: Int,
    val range: String,
    val ritual: Boolean,
    val school: EntityRef,
    val duration: String,
    val castingTime: String,
    val classes: List<EntityRef>,
    val components: List<String>,
    val concentration: Boolean,
    val areaOfEffect: AreaOfEffect? = null,
    val attackType: String? = null,
    val damage: SpellDamage? = null,
    val dc: SpellDC? = null,
    val healAtSlotLevel: Map<String, String>? = null,
    val higherLevel: List<String>? = null,
    val material: String? = null,
    val subclasses: List<EntityRef>? = null,
    val source: String,
)
