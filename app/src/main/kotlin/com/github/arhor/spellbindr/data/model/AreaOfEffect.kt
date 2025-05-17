package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffect(
    val size: Int,
    val type: Type,
) {
    override fun toString(): String = buildString {
        append(size)
        append('-')
        append("foot")
        append(type)
    }

    @Suppress("unused")
    enum class Type {
        CUBE,
        SPHERE,
        CYLINDER,
        CONE,
        LINE,
        CIRCLE,
        ;

        override fun toString(): String = name.lowercase().replaceFirstChar(Char::titlecaseChar)
    }
}
