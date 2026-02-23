package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Speed(
    val burrow: String? = null,
    val climb: String? = null,
    val fly: String? = null,
    val hover: Boolean? = null,
    val swim: String? = null,
    val walk: String? = null
)
