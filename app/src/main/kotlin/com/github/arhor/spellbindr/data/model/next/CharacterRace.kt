package com.github.arhor.spellbindr.data.model.next

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
data class CharacterRace(
    val id: String,
    val name: String,
    val traits: List<Reference>,
    val subraces: List<CharacterSubrace>,
)
