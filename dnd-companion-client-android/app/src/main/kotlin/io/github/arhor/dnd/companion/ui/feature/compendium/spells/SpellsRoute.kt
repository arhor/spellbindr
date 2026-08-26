package io.github.arhor.dnd.companion.ui.feature.compendium.spells

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arhor.dnd.companion.domain.model.Spell
import io.github.arhor.dnd.companion.ui.components.AppTopBarConfig
import io.github.arhor.dnd.companion.ui.components.AppTopBarNavigation
import io.github.arhor.dnd.companion.ui.components.ProvideTopBarState
import io.github.arhor.dnd.companion.ui.components.TopBarState

@Composable
fun SpellsRoute(
    vm: SpellsViewModel = hiltViewModel(),
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Spells",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        SpellsScreen(
            uiState = state,
            dispatch = { intent ->
                when (intent) {
                    is SpellsIntent.SpellClicked -> onSpellSelected(intent.spell)
                    else -> vm.dispatch(intent)
                }
            },
        )
    }
}
