package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a prerequisite required to gain access to a feature, spell,
 * or other element in Dungeons & Dragons 5th edition.
 *
 * Implementations describe different kinds of requirements that must be met.
 */
@Serializable
sealed interface Prerequisite {

    /**
     * Requires a minimum character level to access the feature.
     *
     * @property level The minimum level required.
     */
    @Serializable
    @SerialName("level")
    data class LevelPrerequisite(
        val level: Int,
    ) : Prerequisite

    /**
     * Requires a specific feature to be already known.
     *
     * @property feature The id of the required feature.
     */
    @Serializable
    @SerialName("feature")
    data class FeaturePrerequisite(
        val feature: String,
    ) : Prerequisite

    /**
     * Requires knowledge of a particular spell as a condition.
     *
     * @property spell The id of the required spell.
     */
    @Serializable
    @SerialName("spell")
    data class SpellPrerequisite(
        val spell: String,
    ) : Prerequisite

    /**
     * Requires one or more ability scores to meet or exceed a minimum value.
     *
     * @property abilityScore The list of ability scores checked (e.g., "str", "dex").
     * @property minimumValue The minimum ability score value required.
     * @property atLeastOne If true, only one of the listed ability scores must
     * satisfy the requirement. If false, all listed ability scores must meet
     * or exceed the minimum value.
     */
    @Serializable
    @SerialName("ability-score")
    data class AbilityScorePrerequisite(
        val abilityScore: List<String>,
        val minimumValue: Int,
        val atLeastOne: Boolean = false,
    ) : Prerequisite
}
