package com.github.arhor.spellbindr.data.next.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class OptionSet {

    abstract val optionSetType: OptionSetType

    @Serializable
    @SerialName("equipment_category")
    data class EquipmentCategoryOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.EQUIPMENT_CATEGORY,
        @SerialName("equipment_category")
        val equipmentCategory: EntityRef
    ) : OptionSet()

    @Serializable
    @SerialName("resource_list")
    data class ResourceListOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.RESOURCE_LIST,
        @SerialName("resource_list_name")
        val resourceListName: String
    ) : OptionSet()

    @Serializable
    @SerialName("options_array")
    data class OptionsArrayOptionSet(
        override val optionSetType: OptionSetType = OptionSetType.OPTIONS_ARRAY,
        val options: List<Option>
    ) : OptionSet()
}
