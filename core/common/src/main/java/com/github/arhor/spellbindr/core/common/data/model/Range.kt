package com.github.arhor.spellbindr.core.common.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Range(
    val type: Type,
    val distance: Int? = null,
    val area: AreaOfEffect? = null
) {
    override fun toString(): String = buildString {
        if (distance != null) {
            append(distance)
            append(' ')
        }
        append(type)
        if (area != null) {
            append('(')
            append(area)
            append(')')
        }
    }

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
