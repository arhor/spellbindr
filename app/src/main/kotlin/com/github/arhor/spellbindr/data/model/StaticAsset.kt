package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StaticAsset<T, M>(
    val data: List<T>,
    val meta: M? = null,
)
