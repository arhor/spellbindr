package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.spellpicker.CharacterSpellPickerViewModel.CharacterSpellPickerUiState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
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
        onFavoriteClick = vm::onFavoritesToggled,
        onSpellClick = vm::onSpellSelected,
    )
}

@Composable
private fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onBack: () -> Unit,
    onSourceClassChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onSpellClick: (String) -> Unit,
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
            is CharacterSpellPickerUiState.Loading -> LoadingIndicator()

            is CharacterSpellPickerUiState.Error -> ErrorMessage(state.message)

            is CharacterSpellPickerUiState.Content -> {
                CharacterSpellPickerContent(
                    state = state,
                    onSourceClassChanged = onSourceClassChanged,
                    onQueryChanged = onQueryChanged,
                    onFavoriteClick = onFavoriteClick,
                    onSpellClick = onSpellClick,
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
    onFavoriteClick: () -> Unit,
    onSpellClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.sourceClass,
            onValueChange = { value -> onSourceClassChanged(value) },
            label = { Text("Spellcasting class") },
            placeholder = { Text(text = state.defaultSourceClass.ifBlank { "Spellbook" }) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Tap a spell below to add it to the character.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val spellsState = state.spellsUiState) {
                is SpellsUiState.Loading -> LoadingIndicator()

                is SpellsUiState.Failure -> ErrorMessage(spellsState.errorMessage)

                is SpellsUiState.Content -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SpellSearchInput(
                            query = spellsState.query,
                            onQueryChanged = onQueryChanged,
                            onFiltersClick = {},
                            showFavorite = spellsState.showFavoriteOnly,
                            onFavoriteClick = onFavoriteClick,
                            showFilters = false,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SpellList(
                            spells = spellsState.spells,
                            onSpellClick = { onSpellClick(it.id) },
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
            state = CharacterSpellPickerUiState.Content(
                sourceClass = "Wizard",
                defaultSourceClass = "Wizard",
                spellsUiState = SpellsUiState.Content(
                    query = "",
                    spells = emptyList(),
                    showFavoriteOnly = false,
                    showFilterDialog = false,
                    castingClasses = emptyList(),
                    currentClasses = emptySet(),
                ),
            ),
            onBack = {},
            onSourceClassChanged = {},
            onQueryChanged = {},
            onFavoriteClick = {},
            onSpellClick = {},
        )
    }
}
