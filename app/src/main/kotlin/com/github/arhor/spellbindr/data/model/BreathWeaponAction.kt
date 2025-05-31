package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a breath weapon action that a creature can perform.
 *
 * @property name The name of the breath weapon.
 * @property desc A description of the breath weapon.
 * @property usage Information on how often the breath weapon can be used.
 * @property dc The difficulty class (DC) for saving throws against the breath weapon.
 * @property damage A list of damage types and amounts inflicted by the breath weapon.
 * @property areaOfEffect The area affected by the breath weapon.
 */
@Serializable
data class BreathWeaponAction(
    val name: String,
    val desc: String,
    val usage: Usage,
    val dc: DifficultyClass,
    val damage: List<ActionDamage>,
    val areaOfEffect: AreaOfEffect,
)
