package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

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
            state = state,
            onQueryChanged = vm::onQueryChanged,
            onFiltersClick = vm::onFiltersClick,
            onFavoriteClick = vm::onFavoritesToggled,
            onSpellClick = onSpellSelected,
            onSubmitFilters = vm::onFiltersSubmitted,
            onCancelFilters = vm::onFiltersCanceled,
        )
    }
}
