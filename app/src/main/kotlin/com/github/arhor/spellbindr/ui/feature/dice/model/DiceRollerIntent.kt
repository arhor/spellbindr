package com.github.arhor.spellbindr.ui.feature.dice.model

sealed class DiceRollerIntent {
    data object ToggleCheck : DiceRollerIntent()
    data class SetCheckMode(val mode: CheckMode) : DiceRollerIntent()
    data object IncrementCheckModifier : DiceRollerIntent()
    data object DecrementCheckModifier : DiceRollerIntent()

    data class AddAmountDie(val sides: Int) : DiceRollerIntent()
    data class IncrementAmountDie(val sides: Int) : DiceRollerIntent()
    data class DecrementAmountDie(val sides: Int) : DiceRollerIntent()

    data object ClearAll : DiceRollerIntent()
    data object RollMain : DiceRollerIntent()
    data object RollPercentile : DiceRollerIntent()
    data object ReRollLast : DiceRollerIntent()
}
