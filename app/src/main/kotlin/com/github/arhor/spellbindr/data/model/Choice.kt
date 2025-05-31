package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a choice a player can make, often related to character creation or advancement.
 *
 * @property choose The number of options the player must choose.
 * @property from An [OptionSet] containing the available options for this choice.
 */
@Serializable
data class Choice(
    val choose: Int,
    val from: OptionSet,
)
