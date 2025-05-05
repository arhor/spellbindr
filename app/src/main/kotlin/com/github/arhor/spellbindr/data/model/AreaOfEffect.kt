package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(
    val size: Int,
    val type: Type,
) {
    enum class Type {
        CUBE,
        SPHERE,
        CYLINDER,
        CONE,
        LINE,
        CIRCLE,
    }
}
