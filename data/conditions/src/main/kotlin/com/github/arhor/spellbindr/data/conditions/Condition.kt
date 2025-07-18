package com.github.arhor.spellbindr.data.conditions

import kotlinx.serialization.Serializable

/**
 * Represents a condition that can affect a creature in the game.
 *
 * @property id A unique identifier for the condition.
 * @property name The name of the condition.
 * @property desc A list of strings describing the effects of the condition.
 */
@Serializable
data class Condition(
    val id: String,
    val name: String,
    val desc: List<String>,
)
