package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SuccessType {
    @SerialName("none")
    NONE,

    @SerialName("half")
    HALF,

    @SerialName("other")
    OTHER
}
