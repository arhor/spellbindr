package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Content(
    val item: EntityRef,
    val quantity: Int
)
