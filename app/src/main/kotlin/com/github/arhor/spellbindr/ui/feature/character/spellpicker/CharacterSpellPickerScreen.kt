package com.github.arhor.spellbindr.ui.feature.character.spellpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onSpellcastingClassSelected: (EntityRef) -> Unit = {},
    onSourceClassChanged: (String) -> Unit = {},
    onQueryChanged: (String) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSpellClick: (CharacterSpellAssignment) -> Unit = {},
) {
    when (state) {
        is CharacterSpellPickerUiState.Loading -> LoadingIndicator()

        is CharacterSpellPickerUiState.Content -> CharacterSpellPickerContent(
            state = state,
            onSpellcastingClassSelected = onSpellcastingClassSelected,
            onSourceClassChanged = onSourceClassChanged,
            onQueryChanged = onQueryChanged,
            onFavoriteClick = onFavoriteClick,
            onSpellClick = onSpellClick,
        )

        is CharacterSpellPickerUiState.Failure -> ErrorMessage(state.errorMessage)
    }
}

@Composable
private fun CharacterSpellPickerContent(
    state: CharacterSpellPickerUiState.Content,
    onSpellcastingClassSelected: (EntityRef) -> Unit,
    onSourceClassChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onSpellClick: (CharacterSpellAssignment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.spellcastingClassOptions.isNotEmpty() && state.selectedSpellcastingClass != null) {
            SpellcastingClassSelector(
                spellcastingClassOptions = state.spellcastingClassOptions,
                selectedSpellcastingClass = state.selectedSpellcastingClass,
                onSpellcastingClassSelected = onSpellcastingClassSelected,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = state.sourceClass,
                onValueChange = { value -> onSourceClassChanged(value) },
                label = { Text("Spellcasting class") },
                placeholder = { Text(text = state.defaultSourceClass.ifBlank { "Spellbook" }) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = "Tap a spell below to add it to the character.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SpellSearchInput(
                    query = state.query,
                    onQueryChanged = onQueryChanged,
                    showFavorite = state.showFavoriteOnly,
                    onFavoriteClick = onFavoriteClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
                SpellList(
                    spells = state.spells,
                    onSpellClick = {
                        val resolvedSourceClass = state.selectedSpellcastingClass?.name
                            ?: state.sourceClass.ifBlank { state.defaultSourceClass }
                        onSpellClick(
                            CharacterSpellAssignment(
                                it.id,
                                resolvedSourceClass,
                            )
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpellcastingClassSelector(
    spellcastingClassOptions: List<SpellcastingClassOption>,
    selectedSpellcastingClass: SpellcastingClassOption,
    onSpellcastingClassSelected: (EntityRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedSpellcastingClass.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Spellcasting class") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            spellcastingClassOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        expanded = false
                        onSpellcastingClassSelected(option.id)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
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
                query = "",
                spells = emptyList(),
                showFavoriteOnly = false,
                sourceClass = "",
                defaultSourceClass = "Wizard",
                spellcastingClassOptions = listOf(
                    SpellcastingClassOption(
                        id = EntityRef("wizard"),
                        name = "Wizard",
                    ),
                ),
                selectedSpellcastingClass = SpellcastingClassOption(
                    id = EntityRef("wizard"),
                    name = "Wizard",
                ),
            ),
        )
    }
}
