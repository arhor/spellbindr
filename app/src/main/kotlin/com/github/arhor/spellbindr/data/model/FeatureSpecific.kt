package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import com.github.arhor.spellbindr.domain.model.Choice
import kotlinx.serialization.Serializable


@Serializable
data class FeatureSpecific(
    val subfeatureOptions: Choice? = null,
    val expertiseOptions: Choice? = null,
    val terrainTypeOptions: Choice? = null,
    val enemyTypeOptions: Choice? = null,
    val invocations: List<DomainEntityRef>? = null
)
