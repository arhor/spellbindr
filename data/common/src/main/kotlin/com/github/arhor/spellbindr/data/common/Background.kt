package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

/**
 * Represents a character's background in the game.
 *
 * A background provides additional flavor and mechanical benefits to a character,
 * reflecting their upbringing, profession, or significant life events before becoming
 * an adventurer.
 *
 * @property id A unique identifier for the background.
 * @property name The name of the background (e.g., "Acolyte", "Criminal").
 * @property feature A special feature or ability granted by this background.
 * @property effects A list of effects that this background applies to the character.
 * @property languageChoice Optional choice for additional languages known by the character.
 * @property equipmentChoice Optional choice for additional starting equipment.
 * @property personalityTraits Options for personality traits associated with this background.
 * @property ideals Options for ideals that characters with this background might hold.
 * @property bonds Options for bonds that characters with this background might have.
 * @property flaws Options for flaws that characters with this background might possess.
 */
@Serializable
data class Background(
    val id: String,
    val name: String,
    val feature: GenericInfo,
    val effects: List<Effect>,
    val languageChoice: Choice?,
    val equipmentChoice: Choice?,
    val personalityTraits: Choice,
    val ideals: Choice,
    val bonds: Choice,
    val flaws: Choice,
)
