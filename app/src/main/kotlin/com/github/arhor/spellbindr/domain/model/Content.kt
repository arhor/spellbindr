package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class Content(
    val item: EntityRef,
    val quantity: Int,
)
