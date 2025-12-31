package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun AlignmentsRoute(
    vm: AlignmentsViewModel,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Alignments") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        AlignmentsScreen(
            state = state,
            onAlignmentClick = vm::onAlignmentClick,
        )
    }
}
