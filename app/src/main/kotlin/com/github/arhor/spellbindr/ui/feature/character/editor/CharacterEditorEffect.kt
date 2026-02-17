package com.github.arhor.spellbindr.ui.feature.character.editor

/**
 * Represents one-off UI effects for the Character Editor feature.
 */
sealed interface CharacterEditorEffect {
    /**
     * Effect emitted after a successful save.
     */
    data object Saved : CharacterEditorEffect

    /**
     * Effect emitted when save operation fails.
     */
    data class Error(val message: String) : CharacterEditorEffect
}
