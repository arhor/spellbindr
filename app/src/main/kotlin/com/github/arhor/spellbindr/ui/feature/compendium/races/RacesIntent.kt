package com.github.arhor.spellbindr.ui.feature.compendium.races

/**
 * Represents user intents for the Races screen.
 */
sealed interface RacesIntent {
    /**
     * Intent emitted when a race item is clicked.
     */
    data class RaceClicked(val raceId: String) : RacesIntent
}

/**
 * Dispatch function for [RacesIntent] events.
 */
typealias RacesDispatch = (RacesIntent) -> Unit
