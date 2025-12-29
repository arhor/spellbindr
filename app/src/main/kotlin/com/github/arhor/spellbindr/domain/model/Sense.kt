package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sense(
    val blindsight: String? = null,
    val darkvision: String? = null,
    val passivePerception: Int,
    val tremorSense: String? = null,
    val trueSight: String? = null
)
