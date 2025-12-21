package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


@Serializable
data class GenericProficiency(
    val id: String,
    val name: String,
    val type: String,
    val races: List<DomainEntityRef>? = null,
    val classes: List<DomainEntityRef>? = null,
    val reference: Reference
)
