package com.github.arhor.spellbindr.data.model

data class AreaOfEffect(
    val size: Int,
    val type: Type,
) {
    enum class Type {
        CIRCLE,
        CONE,
        CUBE,
        CYLINDER,
        LINE,
        SPHERE,
    }
}

