package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a condition that can affect a creature in the game.
 *
 * @property id The unique identifier of the condition.
 * @property displayName The name of the condition.
 * @property description A list of strings describing the effects of the condition.
 */
@Serializable
data class Condition(
    val id: String,
    val displayName: String,
    val description: List<String>
)
