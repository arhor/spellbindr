package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun ConditionsRoute(
    vm: ConditionsViewModel,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Conditions") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        ConditionsScreen(
            state = state,
            onConditionClick = vm::onConditionClick,
        )
    }
}
