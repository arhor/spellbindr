package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the shape of an area of effect for a spell or ability.
 *
 * Each type corresponds to a geometric shape used to determine the affected area.
 */
@Serializable
enum class AreaOfEffectType {
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
