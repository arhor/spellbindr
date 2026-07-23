package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a playable race in the game.
 */
@Serializable
data class Race(
    val id: String,
    val name: String,
    val traits: List<EntityRef>,
    val subraces: List<Subrace>,
) {
    @Serializable
    data class Subrace(
        val id: String,
        val name: String,
        val desc: String,
        val traits: List<EntityRef>,
    )
}
