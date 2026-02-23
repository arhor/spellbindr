package com.github.arhor.spellbindr.ui.feature.dice.model

import androidx.compose.runtime.Immutable

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
