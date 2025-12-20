package com.github.arhor.spellbindr.domain.model

/**
 * Represents a playable race in the game.
 */
data class Race(
    val id: String,
    val name: String,
    val traits: List<Reference>,
    val subraces: List<Subrace>,
) {
    data class Subrace(
        val id: String,
        val name: String,
        val desc: String,
        val traits: List<Reference>,
    )
}
