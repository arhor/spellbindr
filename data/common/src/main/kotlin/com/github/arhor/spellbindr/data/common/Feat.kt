package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
data class Feat(
    val id: String,
    val name: String,
    val desc: List<String>,
    val prerequisites: List<FeatPrerequisite>
)
