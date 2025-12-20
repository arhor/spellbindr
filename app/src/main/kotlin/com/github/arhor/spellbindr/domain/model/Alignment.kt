package com.github.arhor.spellbindr.domain.model

/**
 * Represents the moral and ethical stance of a character or creature.
 */
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbr: String,
)
