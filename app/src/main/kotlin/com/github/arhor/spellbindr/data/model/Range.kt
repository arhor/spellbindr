package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val base: Int,
    val long: Int? = null,
)
