package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompendiumSpellsRoute(
    vm: SpellsViewModel,
    onSpellSelected: (Spell) -> Unit,
    onBack: () -> Unit,
) {
    val state = vm.spellsState.collectAsStateWithLifecycle().value

    LaunchedEffect(vm) {
        vm.effects.collectLatest { effect ->
            when (effect) {
                is SpellsViewModel.Effect.SpellSelected -> onSpellSelected(effect.spell)
            }
        }
    }

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
            onQueryChanged = { vm.onAction(SpellsViewModel.Action.QueryChanged(it)) },
            onFiltersClick = { vm.onAction(SpellsViewModel.Action.FiltersClicked) },
            onFavoriteClick = { vm.onAction(SpellsViewModel.Action.FavoritesToggled) },
            onGroupToggle = { vm.onAction(SpellsViewModel.Action.GroupToggled(it)) },
            onToggleAllGroups = { vm.onAction(SpellsViewModel.Action.ToggleAllGroups) },
            onSpellClick = { vm.onAction(SpellsViewModel.Action.SpellClicked(it)) },
            onSubmitFilters = { vm.onAction(SpellsViewModel.Action.FiltersSubmitted(it)) },
            onCancelFilters = { vm.onAction(SpellsViewModel.Action.FiltersCanceled(it)) },
        )
    }
}
