package com.github.arhor.spellbindr.ui.screens.spells.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.components.SpellList

@Composable
fun SpellSearchScreen(
    onSpellClick: (String) -> Unit = {},
    spellSearchVM: SpellSearchViewModel = hiltViewModel(),
) {
    val spellSearchState by spellSearchVM.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Spell Book",
                style = MaterialTheme.typography.titleLarge,
            )
            IconButton(onClick = spellSearchVM::onFavoritesClicked) {
                if (spellSearchState.showFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorites: ON",
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorites: OFF",
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SpellSearchInput(
            query = spellSearchState.query,
            onQueryChanged = spellSearchVM::onQueryChanged,
            onFiltersClick = spellSearchVM::onFilterClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            spellSearchState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            spellSearchState.error != null -> {
                Text(
                    text = "Error: ${spellSearchState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                SpellList(
                    spells = spellSearchState.spells,
                    onSpellClick = onSpellClick,
                )
            }
        }
    }

    SearchFilterDialog(
        showFilterDialog = spellSearchState.showFilterDialog,
        castingClasses = spellSearchState.castingClasses,
        currentClasses = spellSearchState.currentClasses,
        onSubmit = spellSearchVM::onFilterChanged,
        onCancel = spellSearchVM::onFilterChanged,
    )
}
