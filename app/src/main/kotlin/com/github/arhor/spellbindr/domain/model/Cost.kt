package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the monetary cost of equipment or other items.
 *
 * This data class is serializable using Kotlinx Serialization.
 *
 * @property quantity The numerical amount of the currency.
 * @property unit The type of currency (e.g., "gp" for gold pieces, "sp" for silver pieces, "cp" for copper pieces).
 */
@Serializable
data class Cost(
    val quantity: Int,
    val unit: String
)
