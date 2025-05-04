package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Component(
    val type: Type,
    val material: String? = null,
) {
    enum class Type {
        V,
        S,
        M,
    }
}
