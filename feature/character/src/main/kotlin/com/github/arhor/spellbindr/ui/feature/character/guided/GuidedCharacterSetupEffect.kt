package com.github.arhor.spellbindr.ui.feature.character.guided

/**
 * Represents one-off UI effects for the Guided Character Setup feature.
 */
sealed interface GuidedCharacterSetupEffect {
    /**
     * Effect emitted after character creation is completed.
     */
    data class CharacterCreated(val characterId: String) : GuidedCharacterSetupEffect

    /**
     * Effect emitted when character creation fails.
     */
    data class Error(val message: String) : GuidedCharacterSetupEffect
}
