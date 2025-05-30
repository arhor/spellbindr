package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActionItem(
    @SerialName("action_name")
    val actionName: String,
    val count: String,
    val type: String
)
