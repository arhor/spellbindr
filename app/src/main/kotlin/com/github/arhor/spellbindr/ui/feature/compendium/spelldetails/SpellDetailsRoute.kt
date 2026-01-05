package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState

@Composable
fun SpellDetailsRoute(
    vm: SpellDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Spells",
                navigation = AppTopBarNavigation.Back(onBack),
                actions = { ToggleFavoriteSpell(state, vm::toggleFavorite) },
            ),
        ),
    ) {
        SpellDetailScreen(
            state = state,
        )
    }
}

@Composable
private fun ToggleFavoriteSpell(
    state: SpellDetailsUiState,
    onClick: () -> Unit,
) {
    val isEnabled = state is SpellDetailsUiState.Content
    val isFavorite = (state as? SpellDetailsUiState.Content)?.isFavorite == true

    IconButton(
        onClick = onClick,
        enabled = isEnabled,
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
