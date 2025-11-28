package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

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
        SpellSearchInput(
            query = spellSearchState.query,
            onQueryChanged = spellSearchVM::onQueryChanged,
            onFiltersClick = spellSearchVM::onFilterClicked,
            showFavorite = spellSearchState.showFavorite,
            onFavoriteClick = spellSearchVM::onFavoritesClicked,
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
