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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellSearchScreen(
    onSpellClick: (String) -> Unit = {},
    spellSearchVM: SpellSearchViewModel = hiltViewModel(),
) {
    val spellSearchState by spellSearchVM.state.collectAsState()
    SpellSearchContent(
        state = spellSearchState,
        onQueryChanged = spellSearchVM::onQueryChanged,
        onFiltersClick = spellSearchVM::onFilterClicked,
        onFavoriteClick = spellSearchVM::onFavoritesClicked,
        onSpellClick = onSpellClick,
        onSubmitFilters = spellSearchVM::onFilterChanged,
        onCancelFilters = spellSearchVM::onFilterChanged,
    )
}

@Composable
private fun SpellSearchContent(
    state: SpellSearchViewModel.State,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSpellClick: (String) -> Unit,
    onSubmitFilters: (Set<EntityRef>) -> Unit,
    onCancelFilters: (Set<EntityRef>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SpellSearchInput(
            query = state.query,
            onQueryChanged = onQueryChanged,
            onFiltersClick = onFiltersClick,
            showFavorite = state.showFavorite,
            onFavoriteClick = onFavoriteClick,
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                SpellList(
                    spells = state.spells,
                    onSpellClick = onSpellClick,
                )
            }
        }
    }

    SearchFilterDialog(
        showFilterDialog = state.showFilterDialog,
        castingClasses = state.castingClasses,
        currentClasses = state.currentClasses,
        onSubmit = onSubmitFilters,
        onCancel = onCancelFilters,
    )
}

@Preview
@Composable
private fun SpellSearchScreenLightPreview() {
    SpellSearchScreenPreview(isDarkTheme = false)
}

@Preview
@Composable
private fun SpellSearchScreenDarkPreview() {
    SpellSearchScreenPreview(isDarkTheme = true)
}

@Composable
private fun SpellSearchScreenPreview(isDarkTheme: Boolean) {
    val previewSpell = Spell(
        id = "healing_word",
        name = "Healing Word",
        desc = listOf("A creature of your choice that you can see regains hit points."),
        level = 1,
        range = "60 ft",
        ritual = false,
        school = EntityRef(id = "evocation"),
        duration = "Instant",
        castingTime = "1 bonus action",
        classes = listOf(EntityRef(id = "cleric")),
        components = listOf("V"),
        concentration = false,
        source = "PHB",
    )
    AppTheme(isDarkTheme = isDarkTheme) {
        SpellSearchContent(
            state = SpellSearchViewModel.State(
                query = "heal",
                spells = listOf(previewSpell.copy(level = 0, name = "Sacred Flame"), previewSpell),
                castingClasses = listOf(EntityRef(id = "cleric")),
            ),
            onQueryChanged = {},
            onFiltersClick = {},
            onFavoriteClick = {},
            onSpellClick = {},
            onSubmitFilters = {},
            onCancelFilters = {},
        )
    }
}
