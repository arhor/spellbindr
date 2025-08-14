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

    @Serializable
    @SerialName("proficiency")
    data class ProficiencyChoice(
        override val choose: Int,
        val from: List<String>,
    ) : Choice

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

    /**
     * Represents a set of equipment options that a character can choose from.
     *
     * @property choose The number of equipment items the character can choose.
     * @property from A list of strings representing the IDs of the available equipment items.
     */
    @Serializable
    @SerialName("equipment")
    data class EquipmentChoice(
        override val choose: Int,
        val from: List<String>,
    ) : Choice

    /**
     * Represents a set of options where the character can choose equipment from specific categories.
     * This is used when the choices are limited to certain types of equipment.
     *
     * @property choose The number of equipment items the character can choose from the specified categories.
     * @property from An [EquipmentOption] object that defines the categories of equipment to choose from.
     */
    @Serializable
    @SerialName("equipment-categories")
    data class EquipmentCategoriesChoice(
        override val choose: Int,
        val from: EquipmentOption,
    ) : Choice {
        /**
         * Represents an option for choosing equipment based on categories.
         *
         * @property categories A list of strings representing the equipment categories which an equipment
         *                      item must belong to in order to be chosen.
         */
        @Serializable
        data class EquipmentOption(
            val categories: List<String>,
        )
    }

    /**
     * Represents a set of options where the character can choose from a list of other choice sets.
     * This allows for complex, multi-level choices.
     *
     * @property from A list of [Choice] objects representing the nested choice sets.
     */
    @Serializable
    @SerialName("nested-choice")
    data class NestedChoice(
        override val choose: Int,
        val from: List<Choice>,
    ) : Choice

    @Serializable
    @SerialName("from-all")
    data class FromAllChoice(
        override val choose: Int,
    ) : Choice
}
