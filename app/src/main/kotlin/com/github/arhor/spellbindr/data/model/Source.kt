package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val book: String,
    val page: Int,
)
