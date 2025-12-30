package com.github.arhor.spellbindr.ui.feature.compendium.races

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
fun CompendiumRacesRoute(
    vm: CompendiumViewModel,
    onBack: () -> Unit,
) {
    val state = vm.racesState.collectAsStateWithLifecycle().value

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Races") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        RacesRoute(
            state = state,
            onRaceClick = { raceName ->
                vm.onAction(CompendiumAction.RaceClicked(raceName))
            },
        )
    }
}
