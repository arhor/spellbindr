@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerViewModel.CharacterSpellPickerUiState
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerViewModel.SpellsState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CharacterSpellPickerRoute(
    vm: CharacterSpellPickerViewModel,
    onBack: () -> Unit,
    onSpellSelected: (List<CharacterSpellAssignment>) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.spellAssignments.collectLatest { assignment ->
            onSpellSelected(listOf(assignment))
        }
    }

    CharacterSpellPickerScreen(
        state = state,
        onBack = onBack,
        onSourceClassChanged = vm::onSourceClassChanged,
        onQueryChanged = vm::onQueryChanged,
        onFiltersClick = vm::onFiltersClick,
        onFavoriteClick = vm::onFavoritesClick,
        onGroupToggle = vm::onSpellGroupToggled,
        onToggleAllGroups = vm::onToggleAllSpellGroups,
        onSpellClick = vm::onSpellSelected,
        onSubmitFilters = vm::onSubmitFilters,
        onCancelFilters = vm::onCancelFilters,
    )
}

@Composable
private fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onBack: () -> Unit,
    onSourceClassChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onGroupToggle: (Int) -> Unit,
    onToggleAllGroups: () -> Unit,
    onSpellClick: (String) -> Unit,
    onSubmitFilters: (List<EntityRef>) -> Unit,
    onCancelFilters: (List<EntityRef>) -> Unit,
) {
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Add Spells",
                navigation = AppTopBarNavigation.Back(onBack),
            ),
        ),
    ) {
        when (state) {
            is CharacterSpellPickerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is CharacterSpellPickerUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is CharacterSpellPickerUiState.Content -> {
                CharacterSpellPickerContent(
                    state = state,
                    onSourceClassChanged = onSourceClassChanged,
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
        }
    }
}

@Composable
private fun CharacterSpellPickerContent(
    state: CharacterSpellPickerUiState.Content,
    onSourceClassChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFiltersClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onGroupToggle: (Int) -> Unit,
    onToggleAllGroups: () -> Unit,
    onSpellClick: (String) -> Unit,
    onSubmitFilters: (List<EntityRef>) -> Unit,
    onCancelFilters: (List<EntityRef>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.sourceClass,
            onValueChange = { value ->
                onSourceClassChanged(value)
            },
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
            SpellsScreen(
                state = state.spellsState,
                onQueryChanged = onQueryChanged,
                onFiltersClick = onFiltersClick,
                onFavoriteClick = onFavoriteClick,
                onGroupToggle = onGroupToggle,
                onToggleAllGroups = onToggleAllGroups,
                onSpellClick = { spell ->
                    onSpellClick(spell.id)
                },
                onSubmitFilters = onSubmitFilters,
                onCancelFilters = onCancelFilters,
            )
        }
    }
}

@Preview
@Composable
private fun CharacterSpellPickerPreview() {
    AppTheme {
        CharacterSpellPickerScreen(
            state = CharacterSpellPickerUiState.Content(
                sourceClass = "Wizard",
                defaultSourceClass = "Wizard",
                spellsState = SpellsState(),
            ),
            onBack = {},
            onSourceClassChanged = {},
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
