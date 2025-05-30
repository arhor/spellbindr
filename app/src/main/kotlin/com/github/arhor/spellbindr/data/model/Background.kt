package com.github.arhor.spellbindr.data.model

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
 * @property languageOptions Options for additional languages known by the character due to their background.
 * @property startingProficiencies A list of skill or tool proficiencies granted by this background.
 * @property startingEquipment A list of specific equipment items granted by this background.
 * @property startingEquipmentOptions Choices for additional starting equipment.
 * @property feature A special feature or ability granted by this background.
 * @property personalityTraits Options for personality traits associated with this background.
 * @property ideals Options for ideals that characters with this background might hold.
 * @property bonds Options for bonds that characters with this background might have.
 * @property flaws Options for flaws that characters with this background might possess.
 */
@Serializable
data class Background(
    val id: String,
    val name: String,
    val languageOptions: Choice,
    val startingProficiencies: List<EntityRef>,
    val startingEquipment: List<EquipmentRef>,
    val startingEquipmentOptions: List<Choice>,
    val feature: GenericInfo,
    val personalityTraits: Choice,
    val ideals: Choice,
    val bonds: Choice,
    val flaws: Choice,
)
