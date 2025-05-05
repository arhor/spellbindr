package com.github.arhor.spellbindr.data.model

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
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

    enum class Type {
        CUBE,
        SPHERE,
        CYLINDER,
        CONE,
        LINE,
        CIRCLE,
        ;

        override fun toString(): String = super.toString().lowercase().capitalize(Locale.current)
    }
}
