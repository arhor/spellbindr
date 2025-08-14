package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val id: String,
    val name: String,
    val desc: List<String>,
    val featureSpecific: FeatureSpecific? = null
)
