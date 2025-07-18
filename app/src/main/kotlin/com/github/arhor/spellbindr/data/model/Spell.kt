package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.Serializable

/**
 * Represents a spell with its various attributes.
 *
 * @property id The unique identifier for the spell.
 * @property name The name of the spell.
 * @property desc A list of strings describing the spell.
 * @property level The spell's level.
 * @property range The range of the spell.
 * @property ritual Indicates if the spell can be cast as a ritual.
 * @property school A reference to the school of magic the spell belongs to.
 * @property duration The duration of the spell.
 * @property castingTime The time it takes to cast the spell.
 * @property classes A list of references to classes that can use this spell.
 * @property components A list of components required to cast the spell (e.g., "V", "S", "M").
 * @property concentration Indicates if the spell requires concentration.
 * @property areaOfEffect Optional information about the spell's area of effect.
 * @property attackType Optional type of attack the spell performs (e.g., "ranged", "melee").
 * @property damage Optional information about the spell's damage.
 * @property dc Optional information about the spell's saving throw DC.
 * @property healAtSlotLevel Optional map indicating healing amounts at different slot levels.
 * @property higherLevel Optional list of strings describing the effects of casting the spell at higher levels.
 * @property material Optional description of material components required for the spell.
 * @property subclasses Optional list of references to subclasses that can use this spell.
 */
@Serializable
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

