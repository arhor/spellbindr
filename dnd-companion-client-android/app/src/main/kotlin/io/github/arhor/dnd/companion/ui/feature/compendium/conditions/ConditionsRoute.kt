package io.github.arhor.dnd.companion.ui.feature.compendium.conditions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.AppTopBarNavigation
import io.github.arhor.dnd.companion.ui.components.ProvideTopBarState
import io.github.arhor.dnd.companion.ui.components.TopBarState

@Composable
fun ConditionsRoute(
    vm: ConditionsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Conditions",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        ConditionsScreen(
            uiState = state,
            dispatch = vm::dispatch,
        )
    }
}
