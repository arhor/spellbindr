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
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
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
        vm.effects.collectLatest { effect ->
            when (effect) {
                is CharacterSpellPickerViewModel.CharacterSpellPickerEffect.SpellAssignmentReady ->
                    onSpellSelected(listOf(effect.assignment))
            }
        }
    }

    CharacterSpellPickerScreen(
        state = state,
        onBack = onBack,
        onAction = vm::onAction,
    )
}

@Composable
private fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onBack: () -> Unit,
    onAction: (CharacterSpellPickerViewModel.CharacterSpellPickerUiAction) -> Unit,
) {
    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Add Spells") },
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
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun CharacterSpellPickerContent(
    state: CharacterSpellPickerUiState.Content,
    onAction: (CharacterSpellPickerViewModel.CharacterSpellPickerUiAction) -> Unit,
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
                onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.SourceClassChanged(value))
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
            SpellSearchScreen(
                state = state.spellsState,
                onQueryChanged = { query ->
                    onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.QueryChanged(query))
                },
                onFiltersClick = { onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.FiltersClicked) },
                onFavoriteClick = { onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.FavoritesClicked) },
                onGroupToggle = { level ->
                    onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.SpellGroupToggled(level))
                },
                onToggleAllGroups = { onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.ToggleAllSpellGroups) },
                onSpellClick = { spell ->
                    onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.SpellSelected(spell.id))
                },
                onSubmitFilters = { classes: Set<EntityRef> ->
                    onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.FilterChanged(classes))
                },
                onCancelFilters = { classes: Set<EntityRef> ->
                    onAction(CharacterSpellPickerViewModel.CharacterSpellPickerUiAction.FilterChanged(classes))
                },
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
                spellsState = CharacterSpellPickerViewModel.SpellsState(),
            ),
            onBack = {},
            onAction = {},
        )
    }
}
