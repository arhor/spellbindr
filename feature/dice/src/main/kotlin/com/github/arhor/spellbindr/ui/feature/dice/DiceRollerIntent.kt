package com.github.arhor.spellbindr.ui.feature.dice

import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode

/**
 * Represents user intents for the Dice Roller screen.
 */
sealed interface DiceRollerIntent {
    /**
     * Intent emitted when check mode section is toggled.
     */
    data object ToggleCheck : DiceRollerIntent

    /**
     * Intent emitted when check mode is selected.
     */
    data class CheckModeSelected(val mode: CheckMode) : DiceRollerIntent

    /**
     * Intent emitted when check modifier increment is requested.
     */
    data object IncrementCheckModifier : DiceRollerIntent

    /**
     * Intent emitted when check modifier decrement is requested.
     */
    data object DecrementCheckModifier : DiceRollerIntent

    /**
     * Intent emitted when a die type is added to amount dice.
     */
    data class AddAmountDie(val sides: Int) : DiceRollerIntent

    /**
     * Intent emitted when amount die count is incremented.
     */
    data class IncrementAmountDie(val sides: Int) : DiceRollerIntent

    /**
     * Intent emitted when amount die count is decremented.
     */
    data class DecrementAmountDie(val sides: Int) : DiceRollerIntent

    /**
     * Intent emitted when current check and amount configuration should be reset.
     */
    data object ClearAll : DiceRollerIntent

    /**
     * Intent emitted when main roll is requested.
     */
    data object RollMain : DiceRollerIntent

    /**
     * Intent emitted when percentile roll is requested.
     */
    data object RollPercentile : DiceRollerIntent

    /**
     * Intent emitted when reroll of latest result is requested.
     */
    data object RerollLast : DiceRollerIntent
}

/**
 * Dispatch function for [DiceRollerIntent] events.
 */
typealias DiceRollerDispatch = (DiceRollerIntent) -> Unit
