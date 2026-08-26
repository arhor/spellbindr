package com.github.arhor.spellbindr.ui.feature.compendium

/**
 * Represents user intents for the Compendium sections screen.
 */
sealed interface CompendiumIntent {
    /**
     * Intent emitted when a compendium section is selected.
     */
    data class SectionClicked(val section: CompendiumSections) : CompendiumIntent
}

/**
 * Dispatch function for [CompendiumIntent] events.
 */
typealias CompendiumDispatch = (CompendiumIntent) -> Unit
