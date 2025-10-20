package com.github.arhor.spellbindr.ui.feature.diceRoller.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a roll history entry with unique ID
 */
@Serializable
data class RollHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val rollSet: RollSet,
    val timestamp: Long = System.currentTimeMillis()
)
