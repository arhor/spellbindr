package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

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
            onConditionClick = vm::onConditionClick,
        )
    }
}
