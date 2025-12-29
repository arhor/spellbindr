package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AbilityAssetModel(
    val id: String,
    val displayName: String,
    val description: List<String>,
)
