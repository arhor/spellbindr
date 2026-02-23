package com.github.arhor.spellbindr.ui.feature.compendium.conditions

/**
 * Represents user intents for the Conditions screen.
 */
sealed interface ConditionsIntent {
    /**
     * Intent emitted when a condition item is clicked.
     */
    data class ConditionClicked(val conditionId: String) : ConditionsIntent
}

/**
 * Dispatch function for [ConditionsIntent] events.
 */
typealias ConditionsDispatch = (ConditionsIntent) -> Unit
