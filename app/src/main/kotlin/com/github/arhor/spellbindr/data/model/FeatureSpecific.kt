package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureSpecific(
    @SerialName("subfeature_options")
    val subfeatureOptions: Choice? = null,
    val expertiseOptions: Choice? = null,
    @SerialName("terrain_type_options")
    val terrainTypeOptions: Choice? = null,
    @SerialName("enemy_type_options")
    val enemyTypeOptions: Choice? = null,
    val invocations: List<EntityRef>? = null
)
