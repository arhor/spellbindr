package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the usage details of an action, such as how it's recharged or its limited uses.
 *
 * @property type The type of action usage (e.g., "recharge after rest", "per day").
 * @property dice The dice roll involved in the action usage, if any (e.g., "1d6").
 * @property minValue The minimum value required for the action usage, if applicable.
 */
@Serializable
data class ActionUsage(
    val type: String,
    val dice: String? = null,
    val minValue: Int? = null,
    val times: Int? = null,
    val restTypes: List<String>? = null,
)
