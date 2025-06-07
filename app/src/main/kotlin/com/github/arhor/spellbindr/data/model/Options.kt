package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a set of options that a character can choose from.
 * This is a sealed class, meaning that all possible option sets are defined within this file.
 *
 * The different types of option sets are:
 * - [ResourceListOptions]: A set of options where the character can choose from a predefined list of resources.
 * - [OptionsArrayOptions]: A set of options where the character can choose from a list of specific options.
 */
@Serializable
sealed class Options {

    @Serializable
    @SerialName("equipment")
    data class EquipmentOptions(
        val categories: Set<EquipmentCategory>
    ) : Options()

    /**
     * Represents an option set that is a reference to a resource list.
     *
     * @property resourceListName The name of the resource list to reference.
     */
    @Serializable
    @SerialName("resource_list")
    data class ResourceListOptions(
        val resourceListName: String
    ) : Options()

    /**
     * Represents a set of options presented as a list.
     * This is used when the choices are a fixed collection of `Option` objects.
     *
     * @property options A list of [Option] objects available for selection.
     */
    @Serializable
    @SerialName("options_array")
    data class OptionsArrayOptions(
        val options: List<Option>,
    ) : Options()

    @Serializable
    @SerialName("reference")
    data class ReferenceOptions(
        val options: List<EntityRef>,
    ) : Options()
}
