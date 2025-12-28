package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.Serializable


@Serializable
data class Content(
    val item: EntityRef,
    val quantity: Int,
)
