package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

/**
 * Represents one-off UI effects for the Spell Details feature.
 */
sealed interface SpellDetailsEffect {
    /**
     * Effect emitted to display a message to the user.
     */
    data class ShowMessage(val message: String) : SpellDetailsEffect
}
