package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.Serializable


@Serializable
data class GenericProficiency(
    val id: String,
    val name: String,
    val type: String,
    val races: List<EntityRef>? = null,
    val classes: List<EntityRef>? = null,
    val reference: EntityRef
)
