package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef
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
    val traits: List<EntityRef>,
    val subraces: List<Subrace>,
) {
    /**
     * Represents a subrace with a name, description, and a list of traits.
     *
     * @property id The unique identifier for the subrace.
     * @property name The name of the subrace.
     * @property desc A description of the subrace.
     * @property traits A list of trait IDs associated with the subrace.
     */
    @Serializable
    data class Subrace(
        val id: String,
        val name: String,
        val desc: String,
        val traits: List<EntityRef>,
    )
}
