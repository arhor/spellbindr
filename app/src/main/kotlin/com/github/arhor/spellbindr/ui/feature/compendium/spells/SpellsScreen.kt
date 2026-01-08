package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsScreen(
    state: SpellsViewModel.State,
    onQueryChanged: (String) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onGroupToggle: (Int) -> Unit = {},
    onToggleAllGroups: () -> Unit = {},
    onSpellClick: (Spell) -> Unit = {},
    onSubmitFilters: (List<EntityRef>) -> Unit = {},
    onCancelFilters: (List<EntityRef>) -> Unit = {},
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
    state: SpellsViewModel.State,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onGroupToggle: (Int) -> Unit,
    onToggleAllGroups: () -> Unit,
    onSpellClick: (Spell) -> Unit,
    onSubmitFilters: (List<EntityRef>) -> Unit,
    onCancelFilters: (List<EntityRef>) -> Unit,
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
                    text = "Error: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            is SpellsUiState.Content -> {
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

@Composable
@PreviewLightDark
private fun SpellSearchScreenPreview() {
    val spells = listOf(
        Spell(
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
    )
    val spellsByLevel = spells
        .groupBy(Spell::level)
        .toSortedMap()
    AppTheme {
        Surface {
            SpellsScreen(
                state = SpellsViewModel.State(
                    query = "heal",
                    castingClasses = listOf(EntityRef(id = "cleric")),
                    spellsByLevel = spellsByLevel,
                    expandedSpellLevels = mapOf(0 to true, 1 to true),
                    expandedAll = true,
                    uiState = SpellsUiState.Content(
                        query = "heal",
                        spells = spells,
                        spellsByLevel = spellsByLevel,
                    ),
                ),
            )
        }
    }
}
