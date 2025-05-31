package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a choice a player can make, often related to character creation or advancement.
 *
 * @property desc A description of the choice.
 * @property choose The number of options the player must choose.
 * @property type A string indicating the type of choice (e.g., "ability scores", "skill proficiencies").
 * @property from An [OptionSet] containing the available options for this choice.
 */
@Serializable
data class Choice(
    val desc: String,
    val choose: Int,
    val type: String,
    val from: OptionSet,
)
