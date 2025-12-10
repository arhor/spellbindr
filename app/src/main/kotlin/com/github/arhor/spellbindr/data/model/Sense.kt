package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Sense(
    val blindsight: String? = null,
    val darkvision: String? = null,
    val passivePerception: Int,
    val tremorsense: String? = null,
    val truesight: String? = null
)
