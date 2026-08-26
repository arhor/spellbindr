@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.arhor.dnd.companion.ui.feature.dice

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.ProvideTopBarState
import io.github.arhor.dnd.companion.ui.components.TopBarState

@Composable
fun DiceRollerRoute(
    vm: DiceRollerViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Dice Roller",
            ),
        ),
    ) {
        DiceRollerScreen(
            state = state,
            dispatch = vm::dispatch,
        )
    }
}
