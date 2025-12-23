package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.AreaOfEffect
import com.github.arhor.spellbindr.data.model.SpellDamage
import com.github.arhor.spellbindr.data.model.SpellDC

/**
 * Domain representation of a spell.
 *
 * @property id Unique identifier of the spell (slug).
 * @property name Display name of the spell.
 * @property desc List of description paragraphs.
 * @property level Spell level (0 for Cantrip).
 * @property range Casting range (e.g. "Self", "60 feet").
 * @property ritual Whether the spell can be cast as a ritual.
 * @property school The magic school (e.g. Evocation).
 * @property duration Duration of the spell (e.g. "Instantaneous").
 * @property castingTime Time required to cast (e.g. "1 action").
 * @property classes List of classes that can learn this spell.
 * @property components List of components required (V, S, M).
 * @property concentration Whether the spell requires concentration.
 * @property areaOfEffect Optional area of effect details.
 * @property attackType Type of attack (e.g. "ranged", "melee"), if any.
 * @property damage Optional damage details.
 * @property dc Optional saving throw Difficulty Class type.
 * @property healAtSlotLevel Map of healing amounts per slot level.
 * @property higherLevel Description of effects at higher levels.
 * @property material Material component description.
 * @property subclasses List of subclasses that grant this spell.
 * @property source Source book or reference.
 */
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
