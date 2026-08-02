package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * A selectable feat from the bundled rules data.
 *
 * Only [effects] are intentionally machine-readable. Descriptive rules remain in [desc] and are
 * shown to the player without being inferred by progression calculation.
 */
@Serializable
data class Feat(
    val id: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<Prerequisite> = emptyList(),
    val effects: List<Effect> = emptyList(),
    val abilityBonusChoice: Choice.AbilityBonusChoice? = null,
) {
    /** Stable persisted key for the feat-owned ability choice, when the feat has one. */
    val abilityBonusChoiceId: String?
        get() = abilityBonusChoice?.let { "$id:ability-bonus" }
}
