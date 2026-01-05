package com.github.arhor.spellbindr.ui.feature.compendium.spells

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SearchFilterDialog
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellListState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsScreen(
    state: SpellListState,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onGroupToggle: (Int) -> Unit,
    onToggleAllGroups: () -> Unit,
    onSpellClick: (Spell) -> Unit,
    onSubmitFilters: (Set<EntityRef>) -> Unit,
    onCancelFilters: (Set<EntityRef>) -> Unit,
) {
    SpellSearchContent(
        state = state,
        onQueryChanged = onQueryChanged,
        onFiltersClick = onFiltersClick,
        onFavoriteClick = onFavoriteClick,
        onGroupToggle = onGroupToggle,
        onToggleAllGroups = onToggleAllGroups,
        onSpellClick = onSpellClick,
        onSubmitFilters = onSubmitFilters,
        onCancelFilters = onCancelFilters,
    )
}

@Composable
private fun SpellSearchContent(
    state: SpellListState,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onGroupToggle: (Int) -> Unit,
    onToggleAllGroups: () -> Unit,
    onSpellClick: (Spell) -> Unit,
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

        when (val uiState = state.uiState) {
            SpellsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SpellsUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            is SpellsUiState.Loaded -> {
                SpellList(
                    spellsByLevel = state.spellsByLevel,
                    expandedSpellLevels = state.expandedSpellLevels,
                    expandedAll = state.expandedAll,
                    onGroupToggle = onGroupToggle,
                    onToggleAll = onToggleAllGroups,
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

@PreviewLightDark
@Composable
private fun SpellSearchScreenPreview() {
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
    AppTheme {
        SpellSearchContent(
            state = SpellsViewModel.State(
                query = "heal",
                castingClasses = listOf(EntityRef(id = "cleric")),
                spellsByLevel = listOf(previewSpell.copy(level = 0, name = "Sacred Flame"), previewSpell)
                    .groupBy(Spell::level)
                    .toSortedMap(),
                expandedSpellLevels = mapOf(0 to true, 1 to true),
                expandedAll = true,
                uiState = SpellsUiState.Loaded(
                    spells = listOf(previewSpell.copy(level = 0, name = "Sacred Flame"), previewSpell),
                    spellsByLevel = listOf(previewSpell.copy(level = 0, name = "Sacred Flame"), previewSpell)
                        .groupBy(Spell::level)
                        .toSortedMap(),
                ),
            ),
            onQueryChanged = {},
            onFiltersClick = {},
            onFavoriteClick = {},
            onGroupToggle = {},
            onToggleAllGroups = {},
            onSpellClick = {},
            onSubmitFilters = {},
            onCancelFilters = {},
        )
    }
}
