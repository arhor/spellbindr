package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun AlignmentsRoute(
    vm: AlignmentsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Alignments",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        AlignmentsScreen(
            uiState = state,
            dispatch = vm::dispatch,
        )
    }
}
