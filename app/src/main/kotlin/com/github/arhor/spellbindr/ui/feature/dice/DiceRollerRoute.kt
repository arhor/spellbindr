@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.dice

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.dice.model.CheckMode
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerUiState

@Composable
fun DiceRollerRoute(
    vm: DiceRollerViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Dice Roller",
                actions = {
                    IconButton(onClick = { /* Stub: future history action */ }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "History",
                        )
                    }
                },
            ),
        ),
    ) {
        DiceRollerScreen(
            state = state,
            onToggleCheck = vm::toggleCheck,
            onCheckModeSelected = vm::setCheckMode,
            onIncrementCheckModifier = vm::incrementCheckModifier,
            onDecrementCheckModifier = vm::decrementCheckModifier,
            onAddAmountDie = vm::addAmountDie,
            onIncrementAmountDie = vm::incrementAmountDie,
            onDecrementAmountDie = vm::decrementAmountDie,
            onClearAll = vm::clearAll,
            onRollMain = vm::rollMain,
            onRollPercentile = vm::rollPercentile,
            onReRollLast = vm::rerollLast,
        )
    }
}
