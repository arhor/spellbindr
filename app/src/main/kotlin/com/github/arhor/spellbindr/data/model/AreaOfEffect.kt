package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the area of effect of a spell or ability.
 *
 * @property size The size of the area of effect (e.g., radius, length). The unit depends on the [type].
 * @property type The shape or type of the area of effect.
 */
@Serializable
data class AreaOfEffect(
    val size: Int,
    val type: Type,
) {
    /**
     * Represents the shape of an area of effect for a spell or ability.
     *
     * Each type corresponds to a geometric shape used to determine the affected area.
     */
    @Serializable
    enum class Type {
        @SerialName("cone")
        CONE,

        @SerialName("cube")
        CUBE,

        @SerialName("line")
        LINE,

        @SerialName("sphere")
        SPHERE,

        @SerialName("circle")
        CIRCLE,

        @SerialName("cylinder")
        CYLINDER,
    }
}
