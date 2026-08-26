package io.github.arhor.dnd.companion.ui.feature.character.sheet

/**
 * Represents one-off UI effects for the Character Sheet feature.
 */
sealed interface CharacterSheetEffect {
    /**
     * Effect emitted when a character is deleted.
     */
    data object CharacterDeleted : CharacterSheetEffect
}
