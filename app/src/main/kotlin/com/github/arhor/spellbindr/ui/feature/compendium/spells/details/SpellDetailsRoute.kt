package com.github.arhor.spellbindr.ui.feature.compendium.spells.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun SpellDetailRoute(
    vm: SpellDetailsViewModel,
    spellId: String,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val isFavorite = (state as? SpellDetailsViewModel.UiState.Loaded)?.isFavorite == true
    val isFavoriteEnabled = state is SpellDetailsViewModel.UiState.Loaded

    LaunchedEffect(spellId) {
        vm.loadSpell(spellId)
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = { Text("Spells") },
                navigation = AppTopBarNavigation.Back(onBack),
                actions = { ToggleFavoriteSpell(vm, isFavoriteEnabled, isFavorite) },
            ),
        ),
    ) {
        SpellDetailScreen(
            uiState = state,
        )
    }
}

@Composable
private fun ToggleFavoriteSpell(
    vm: SpellDetailsViewModel,
    isFavoriteEnabled: Boolean,
    isFavorite: Boolean
) {
    IconButton(
        onClick = vm::toggleFavorite,
        enabled = isFavoriteEnabled,
    ) {
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Remove from favorites",
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Add to favorites",
            )
        }
    }
}
