package com.github.arhor.spellbindr.data.next.model

import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(
    val size: Int,
    val type: AreaOfEffectType
)
