package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbilityAssetModel(
    val id: String,
    @SerialName("displayName")
    val name: String,
    val description: List<String>,
)
