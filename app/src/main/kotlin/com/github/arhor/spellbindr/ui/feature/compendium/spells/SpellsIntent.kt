package com.github.arhor.spellbindr.ui.feature.compendium.spells

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell

/**
 * Represents user intents for the Spells screen.
 */
sealed interface SpellsIntent {
    /**
     * Intent emitted when search query text changes.
     */
    data class QueryChanged(val query: String) : SpellsIntent

    /**
     * Intent emitted when favorites-only filter is toggled.
     */
    data object FavoritesToggled : SpellsIntent

    /**
     * Intent emitted when a class filter chip is toggled.
     */
    data class ClassFilterToggled(val spellClass: EntityRef) : SpellsIntent

    /**
     * Intent emitted when a spell item is clicked.
     */
    data class SpellClicked(val spell: Spell) : SpellsIntent
}

/**
 * Dispatch function for [SpellsIntent] events.
 */
typealias SpellsDispatch = (SpellsIntent) -> Unit
