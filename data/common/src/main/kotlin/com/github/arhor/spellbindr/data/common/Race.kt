package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

/**
 * Represents a playable race in the game.
 *
 * @property id The unique identifier of the race.
 * @property name The name of the race.
 * @property traits A list of trait IDs associated with the race.
 * @property subraces A list of subraces available for this race.
 */
@Serializable
data class Race(
    val id: String,
    val name: String,
    val traits: List<String>,
    val subraces: List<Subrace>,
)
