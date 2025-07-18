package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sense(
    val blindsight: String? = null,
    val darkvision: String? = null,
    @SerialName("passive_perception")
    val passivePerception: Int,
    val tremorsense: String? = null,
    val truesight: String? = null
)
