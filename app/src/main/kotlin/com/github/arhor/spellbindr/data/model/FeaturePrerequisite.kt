package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FeaturePrerequisite(
    val type: String,
    val feature: String
)
