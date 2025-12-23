package com.github.arhor.spellbindr.domain.model

/**
 * Represents an ability score in Dungeons & Dragons.
 *
 * Each ability score describes a fundamental aspect of a character or monster,
 * influencing their capabilities in various situations.
 *
 * @property displayName The full, display-friendly name of the ability score.
 * @property description A detailed explanation of what the ability score measures and how it's used.
 */
data class Ability(
    val id: String,
    val displayName: String,
    val description: List<String>,
)

