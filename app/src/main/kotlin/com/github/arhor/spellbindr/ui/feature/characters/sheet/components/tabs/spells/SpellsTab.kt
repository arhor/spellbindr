package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellsTabState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsTab(
    spellsState: SpellsTabState,
    editMode: SheetEditMode,
    onAddSpellsClick: () -> Unit,
    onSpellSlotToggle: (Int, Int) -> Unit,
    onSpellSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSpells = spellsState.spellcastingClasses.isNotEmpty()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CastingDashboardCard(
                sharedSlots = spellsState.sharedSlots,
                pactSlots = spellsState.pactSlots,
                concentration = spellsState.concentration,
                editMode = editMode,
                onSharedSlotToggle = onSpellSlotToggle,
                onSharedSlotTotalChanged = onSpellSlotTotalChanged,
                onPactSlotToggle = onPactSlotToggle,
                onPactSlotTotalChanged = onPactSlotTotalChanged,
                onConcentrationClear = onConcentrationClear,
            )
        }
        if (spellsState.canAddSpells) {
            item {
                FilledTonalButton(
                    onClick = onAddSpellsClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add spells")
                }
            }
        }
        if (!hasSpells) {
            item {
                Text(
                    text = "No spells linked to this character yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        items(spellsState.spellcastingClasses, key = { it.sourceKey }) { spellcastingClass ->
            SpellcastingClassCard(
                spellcastingClass = spellcastingClass,
                editMode = editMode,
                onSpellSelected = onSpellSelected,
                onSpellRemoved = onSpellRemoved,
            )
        }
    }
}

@Composable
@PreviewLightDark
internal fun SpellsTabPreview() {
    AppTheme {
        SpellsTab(
            spellsState = CharacterSheetPreviewData.spells,
            editMode = SheetEditMode.View,
            onAddSpellsClick = {},
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onPactSlotToggle = {},
            onPactSlotTotalChanged = {},
            onConcentrationClear = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
        )
    }
}
