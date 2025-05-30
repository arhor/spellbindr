package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OptionSetType {
    @SerialName("equipment_category")
    EQUIPMENT_CATEGORY,

    @SerialName("resource_list")
    RESOURCE_LIST,

    @SerialName("options_array")
    OPTIONS_ARRAY
}
