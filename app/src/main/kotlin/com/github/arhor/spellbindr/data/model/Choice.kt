package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a set of options that a character can choose from.
 * This is a sealed class, meaning that all possible option sets are defined within this file.
 *
 * The different types of option sets are:
 * - [ResourceListChoice]: A set of options where the character can choose from a predefined list of resources.
 * - [OptionsArrayChoice]: A set of options where the character can choose from a list of specific options.
 *
 * @property choose The number of options the character can choose.
 */
@Serializable
sealed interface Choice {
    val choose: Int

    /**
     * Represents a set of options where the character can choose from a predefined list of resources.
     *
     * @property from A string representing the name of the resource list.
     */
    @Serializable
    @SerialName("resource-list")
    data class ResourceListChoice(
        override val choose: Int,
        val from: String,
    ) : Choice

    /**
     * Represents a set of options presented as an array of strings.
     * This is used when the choices are dynamically generated or selected from a larger set.
     *
     * @property from A list of strings representing the available options.
     * @property desc An optional description of the options.
     * @property where An optional map of conditions that filter the options.
     *                 The keys represent property names, and the values represent the required value.
     */
    @Serializable
    @SerialName("options-array")
    data class OptionsArrayChoice(
        override val choose: Int,
        val from: List<String>,
        val desc: String? = null,
        val where: Map<String, String>? = null,
    ) : Choice

    @Serializable
    @SerialName("ability-bonus")
    data class AbilityBonusChoice(
        override val choose: Int,
        val from: List<Map<String, Int>>,
    ) : Choice

    @Serializable
    @SerialName("ideal")
    data class IdealChoice(
        override val choose: Int,
        val from: List<IdealOption>,
    ) : Choice {
        @Serializable
        data class IdealOption(
            val desc: String,
            val alignments: List<String>
        )
    }

    @Serializable
    @SerialName("equipment")
    data class EquipmentChoice(
        override val choose: Int,
        val from: EquipmentOption,
    ) : Choice {
        @Serializable
        data class EquipmentOption(
            val categories: List<String>,
        )
    }
}
