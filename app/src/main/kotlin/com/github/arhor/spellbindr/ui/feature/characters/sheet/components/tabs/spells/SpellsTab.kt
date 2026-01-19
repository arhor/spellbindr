package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSourceFilterUiModel
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
    onSourceFilterSelected: (String?) -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSpells = spellsState.spellLevels.any { it.spells.isNotEmpty() }
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
        if (spellsState.showSourceFilters) {
            item {
                SourceFilterRow(
                    filters = spellsState.sourceFilters,
                    selectedSourceId = spellsState.selectedSourceId,
                    onFilterSelected = onSourceFilterSelected,
                )
            }
        }
        if (!hasSpells) {
            item {
                Text(
                    text = if (spellsState.selectedSourceId == null) {
                        "No spells linked to this character yet."
                    } else {
                        "No spells for this source yet."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        items(spellsState.spellLevels, key = { it.level }) { spellLevel ->
            SpellLevelCard(
                spellLevel = spellLevel,
                editMode = editMode,
                showSourceBadges = spellsState.showSourceBadges,
                onSpellSelected = onSpellSelected,
                onSpellRemoved = onSpellRemoved,
            )
        }
    }
}

@Composable
private fun SourceFilterRow(
    filters: List<SpellSourceFilterUiModel>,
    selectedSourceId: String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            val selected = filter.id == selectedSourceId
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter.id) },
                label = { Text(filter.label) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
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
            onSourceFilterSelected = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
        )
    }
}
