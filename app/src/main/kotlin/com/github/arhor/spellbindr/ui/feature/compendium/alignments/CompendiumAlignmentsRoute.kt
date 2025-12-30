package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumAction

@Composable
fun CompendiumAlignmentsRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state = vm.alignmentsState.collectAsStateWithLifecycle().value

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Alignments") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        AlignmentsRoute(
            state = state,
            onAlignmentClick = { name ->
                vm.onAction(CompendiumAction.AlignmentClicked(name))
            },
        )
    }
}
