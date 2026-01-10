package com.github.arhor.spellbindr.ui.feature.characters.list.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Display model for a single character item in the list.
 */
@Immutable
@Serializable
data class CharacterListItem(
    val id: String,
    val name: String,
    val level: Int,
    val className: String,
    val race: String,
    val background: String,
)
