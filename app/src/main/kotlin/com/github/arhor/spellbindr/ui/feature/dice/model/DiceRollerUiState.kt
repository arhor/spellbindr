package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

sealed interface DiceRollerUiState {

    @Immutable
    data object Loading : DiceRollerUiState

    @Immutable
    data class Content(
        val hasCheck: Boolean = false,
        val checkMode: CheckMode = CheckMode.NORMAL,
        val checkModifier: Int = 0,
        val amountDice: List<DiceGroup> = emptyList(),
        val lastPercentileRoll: Int? = null,
        val latestResult: RollResult? = null,
        val lastRollConfig: RollConfiguration? = null,
        val latestResultToken: Long = 0L,
    ) : DiceRollerUiState

    @Immutable
    data class Error(
        val errorMessage: String,
    ) : DiceRollerUiState
}

@Immutable
enum class CheckMode(val label: String) {
    NORMAL("Normal"),
    ADVANTAGE("Advantage"),
    DISADVANTAGE("Disadvantage"),
}

@Immutable
data class DiceGroup(
    val sides: Int,
    val count: Int,
)

@Immutable
data class RollConfiguration(
    val hasCheck: Boolean,
    val checkMode: CheckMode,
    val checkModifier: Int,
    val amountDice: List<DiceGroup>,
)

@Immutable
sealed class RollResult {

    @Immutable
    data class CheckAmountResult(
        val check: CheckResult?,
        val amount: AmountResult?,
    ) : RollResult()

    @Immutable
    data class PercentileResult(
        val value: Int,
    ) : RollResult()
}

@Immutable
data class CheckResult(
    val mode: CheckMode,
    val rolls: List<Int>,
    val modifier: Int,
    val keptRoll: Int,
    val total: Int,
)

@Immutable
data class AmountResult(
    val groups: List<DiceGroupResult>,
    val total: Int,
)

@Immutable
data class DiceGroupResult(
    val sides: Int,
    val rolls: List<Int>,
) {
    val count: Int get() = rolls.size
    val subtotal: Int get() = rolls.sum()
}

val DiceRollerUiState.Content.canRollMain: Boolean
    get() = hasCheck || amountDice.isNotEmpty()
