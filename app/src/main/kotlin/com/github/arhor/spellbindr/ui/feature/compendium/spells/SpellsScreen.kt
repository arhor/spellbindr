package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SearchFilterDialog
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsScreen(
    state: SpellsUiState,
    onQueryChanged: (String) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSpellClick: (Spell) -> Unit = {},
    onSubmitFilters: (List<EntityRef>) -> Unit = {},
    onCancelFilters: (List<EntityRef>) -> Unit = {},
) {
    when (state) {
        is SpellsUiState.Loading -> LoadingIndicator()
        is SpellsUiState.Failure -> ErrorMessage(state.errorMessage)
        is SpellsUiState.Content -> SpellSearchContent(
            state = state,
            onQueryChanged = onQueryChanged,
            onFiltersClick = onFiltersClick,
            onFavoriteClick = onFavoriteClick,
            onSpellClick = onSpellClick,
            onSubmitFilters = onSubmitFilters,
            onCancelFilters = onCancelFilters,
        )
    }
}

@Composable
private fun SpellSearchContent(
    state: SpellsUiState.Content,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
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
            showFavorite = state.showFavoriteOnly,
            onFavoriteClick = onFavoriteClick,
        )
        Spacer(modifier = Modifier.height(16.dp))

        SpellList(
            spells = state.spells,
            onSpellClick = onSpellClick,
        )
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
    AppTheme {
        SpellsScreen(
            state = SpellsUiState.Content(
                query = "heal",
                castingClasses = listOf(EntityRef(id = "cleric")),
                spells = listOf(
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
                ),
                showFavoriteOnly = false,
                showFilterDialog = false,
                currentClasses = emptyList(),
            ),
        )
    }
}
