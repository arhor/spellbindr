package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode

@Composable
fun SpellsTab(
    spellsState: SpellsTabState,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FilledTonalButton(
                onClick = callbacks.onAddSpellsClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add spells")
            }
        }
        item {
            SpellSlotsCard(
                slots = spellsState.spellSlots,
                editMode = editMode,
                callbacks = callbacks,
            )
        }
        if (spellsState.spellcastingGroups.isEmpty()) {
            item {
                Text(
                    text = "No spells linked to this character yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        } else {
            items(spellsState.spellcastingGroups) { group ->
                SpellGroupCard(
                    group = group,
                    editMode = editMode,
                    callbacks = callbacks,
                )
            }
        }
    }
}
