package io.github.arhor.dnd.companion.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterCreationResult(
    val sheet: CharacterSheet,
    val progression: CharacterProgression,
)
