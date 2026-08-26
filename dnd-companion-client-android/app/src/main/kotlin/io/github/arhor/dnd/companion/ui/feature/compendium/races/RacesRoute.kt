package io.github.arhor.dnd.companion.ui.feature.compendium.races

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.AppTopBarNavigation
import io.github.arhor.dnd.companion.ui.components.ProvideTopBarState
import io.github.arhor.dnd.companion.ui.components.TopBarState

@Composable
fun RacesRoute(
    vm: RacesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Races",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        RacesScreen(
            uiState = state,
            dispatch = vm::dispatch,
        )
    }
}
