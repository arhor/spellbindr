package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ActionItem(
    val actionName: String,
    val count: String,
    val type: String
)
