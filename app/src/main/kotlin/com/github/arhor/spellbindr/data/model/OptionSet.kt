package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OptionSet {

    @Serializable
    @SerialName("equipment_category")
    data class EquipmentCategoryOptionSet(
        @SerialName("equipment_category")
        val equipmentCategory: EntityRef
    ) : OptionSet()

    @Serializable
    @SerialName("resource_list")
    data class ResourceListOptionSet(
        val resourceListName: String
    ) : OptionSet()

    @Serializable
    @SerialName("options_array")
    data class OptionsArrayOptionSet(
        val options: List<Option>,
    ) : OptionSet()
}
