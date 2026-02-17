package com.github.arhor.spellbindr.ui.feature.dice

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceGroup
import com.github.arhor.spellbindr.ui.feature.dice.model.RollConfiguration
import com.github.arhor.spellbindr.ui.feature.dice.model.RollResult

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
    ) : DiceRollerUiState {

        val canRollMain: Boolean
            get() = hasCheck || amountDice.isNotEmpty()
    }

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : DiceRollerUiState
}
