package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterCreationResult(
    val sheet: CharacterSheet,
    val progression: CharacterProgression,
)
