package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel.CompendiumAction
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen

@Composable
fun CompendiumSpellsRoute(
    vm: CompendiumViewModel,
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state = vm.spellsState.collectAsStateWithLifecycle().value

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Spells") },
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        SpellSearchScreen(
            state = state,
            onQueryChanged = { vm.onAction(CompendiumAction.SpellQueryChanged(it)) },
            onFiltersClick = { vm.onAction(CompendiumAction.SpellFiltersClicked) },
            onFavoriteClick = { vm.onAction(CompendiumAction.SpellFavoritesToggled) },
            onGroupToggle = { vm.onAction(CompendiumAction.SpellGroupToggled(it)) },
            onToggleAllGroups = { vm.onAction(CompendiumAction.SpellToggleAllGroups) },
            onSpellClick = onSpellSelected,
            onSubmitFilters = { vm.onAction(CompendiumAction.SpellFiltersSubmitted(it)) },
            onCancelFilters = { vm.onAction(CompendiumAction.SpellFiltersCanceled(it)) },
        )
    }
}
