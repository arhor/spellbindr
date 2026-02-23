package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the moral and ethical stance of a character or creature.
 */
@Serializable
data class Alignment(
    val id: String,
    val name: String,
    val desc: String,
    val abbr: String,
)
