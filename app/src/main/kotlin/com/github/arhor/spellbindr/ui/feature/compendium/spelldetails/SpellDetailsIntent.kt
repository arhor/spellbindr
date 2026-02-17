package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

/**
 * Represents user intents for the Spell Details screen.
 */
sealed interface SpellDetailsIntent {
    /**
     * Intent emitted when favorite state should be toggled.
     */
    data object ToggleFavorite : SpellDetailsIntent
}

/**
 * Dispatch function for [SpellDetailsIntent] events.
 */
typealias SpellDetailsDispatch = (SpellDetailsIntent) -> Unit
