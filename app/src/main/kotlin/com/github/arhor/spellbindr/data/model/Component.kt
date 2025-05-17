package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Component(
    val type: Type,
    val material: String? = null,
) {
    override fun toString(): String = buildString {
        append(type)
        if (material != null) {
            append(" ($material)")
        }
    }

    enum class Type {
        V,
        S,
        M,
    }
}
