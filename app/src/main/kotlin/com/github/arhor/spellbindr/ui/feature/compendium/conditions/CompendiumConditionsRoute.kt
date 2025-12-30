package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumAction

@Composable
fun CompendiumConditionsRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state = vm.conditionsState.collectAsStateWithLifecycle().value

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Conditions") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        ConditionsRoute(
            state = state,
            onConditionClick = { condition: Condition ->
                vm.onAction(CompendiumAction.ConditionClicked(condition))
            },
        )
    }
}
