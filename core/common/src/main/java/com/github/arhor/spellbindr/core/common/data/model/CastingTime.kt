package com.github.arhor.spellbindr.core.common.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CastingTime(
    val amount: Int,
    val unit: TimeUnit,
)


