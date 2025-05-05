package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val type: Type,
    val distance: Int? = null,
    val area: AreaOfEffect? = null
) {
    enum class Type {
        Touch,
        Sight,
        Unlimited,
        Self,
        Feet,
        Miles,
        Special,
    }
}
