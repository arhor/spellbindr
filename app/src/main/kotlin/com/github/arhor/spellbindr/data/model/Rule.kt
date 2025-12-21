package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


@Serializable
data class Rule(
    val id: String,
    val name: String,
    val desc: String,
    val subsections: List<DomainEntityRef>
)
