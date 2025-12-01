@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchViewModel
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSpellPickerRoute(
    state: CharacterSpellPickerUiState,
    spellSearchState: SpellSearchViewModel.State,
    onBack: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSourceChanged: (String) -> Unit,
    onSpellQueryChanged: (String) -> Unit,
    onSpellFiltersClick: () -> Unit,
    onSpellFavoriteClick: () -> Unit,
    onSpellSubmitFilters: (Set<EntityRef>) -> Unit,
    onSpellCancelFilters: (Set<EntityRef>) -> Unit,
    modifier: Modifier = Modifier,
) {
    CharacterSpellPickerScreen(
        state = state,
        onBack = onBack,
        onSourceChanged = onSourceChanged,
        onSpellSelected = onSpellSelected,
        spellSearchState = spellSearchState,
        onSpellQueryChanged = onSpellQueryChanged,
        onSpellFiltersClick = onSpellFiltersClick,
        onSpellFavoriteClick = onSpellFavoriteClick,
        onSpellSubmitFilters = onSpellSubmitFilters,
        onSpellCancelFilters = onSpellCancelFilters,
        modifier = modifier,
    )
}

@Composable
private fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onBack: () -> Unit,
    onSourceChanged: (String) -> Unit,
    onSpellSelected: (String) -> Unit,
    spellSearchState: SpellSearchViewModel.State,
    onSpellQueryChanged: (String) -> Unit,
    onSpellFiltersClick: () -> Unit,
    onSpellFavoriteClick: () -> Unit,
    onSpellSubmitFilters: (Set<EntityRef>) -> Unit,
    onSpellCancelFilters: (Set<EntityRef>) -> Unit,
    modifier: Modifier = Modifier,
) {
    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text("Add Spells") },
            navigation = AppTopBarNavigation.Back(onBack),
        )
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.sourceClass,
                        onValueChange = onSourceChanged,
                        label = { Text("Spellcasting class") },
                        placeholder = { Text(text = state.defaultSourceClass.ifBlank { "Spellbook" }) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "Tap a spell below to add it to the character.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        SpellSearchScreen(
                            state = spellSearchState,
                            onQueryChanged = onSpellQueryChanged,
                            onFiltersClick = onSpellFiltersClick,
                            onFavoriteClick = onSpellFavoriteClick,
                            onSpellClick = onSpellSelected,
                            onSubmitFilters = onSpellSubmitFilters,
                            onCancelFilters = onSpellCancelFilters,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CharacterSpellPickerPreview() {
    AppTheme {
        CharacterSpellPickerScreen(
            state = CharacterSpellPickerUiState(
                sourceClass = "Wizard",
                defaultSourceClass = "Wizard",
            ),
            onBack = {},
            onSourceChanged = {},
            onSpellSelected = {},
            spellSearchState = SpellSearchViewModel.State(),
            onSpellQueryChanged = {},
            onSpellFiltersClick = {},
            onSpellFavoriteClick = {},
            onSpellSubmitFilters = {},
            onSpellCancelFilters = {},
        )
    }
}
