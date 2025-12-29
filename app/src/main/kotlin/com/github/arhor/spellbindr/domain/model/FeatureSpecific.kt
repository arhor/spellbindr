package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class FeatureSpecific(
    val subfeatureOptions: Choice? = null,
    val expertiseOptions: Choice? = null,
    val terrainTypeOptions: Choice? = null,
    val enemyTypeOptions: Choice? = null,
    val invocations: List<EntityRef>? = null
)
