package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CastingTime(
    val amount: Int,
    val unit: TimeUnit,
)


