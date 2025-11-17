package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

@Composable
private fun SpellSlotsCard(
    slots: List<SpellSlotUiModel>,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spell Slots",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                slots.forEach { slot ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Level ${slot.level}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (editMode == SheetEditMode.Editing) {
                                Row {
                                    IconButton(
                                        onClick = { callbacks.onSpellSlotTotalChanged(slot.level, slot.total - 1) },
                                        enabled = slot.total > 0,
                                    ) {
                                        Text("-")
                                    }
                                    IconButton(
                                        onClick = { callbacks.onSpellSlotTotalChanged(slot.level, slot.total + 1) },
                                    ) {
                                        Text("+")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (slot.total == 0) {
                            Text(
                                text = "No slots configured for this level",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                repeat(slot.total) { index ->
                                    val used = index < slot.expended
                                    SpellSlotChip(
                                        used = used,
                                        onClick = { callbacks.onSpellSlotToggle(slot.level, index) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellSlotChip(
    used: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        tonalElevation = if (used) 4.dp else 0.dp,
        color = if (used) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (used) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun SpellGroupCard(
    group: SpellcastingGroupUiModel,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.sourceLabel,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            group.spells.forEach { spell ->
                SpellRow(
                    spell = spell,
                    editMode = editMode,
                    onClick = { callbacks.onSpellSelected(spell.spellId) },
                    onRemove = { callbacks.onSpellRemoved(spell.spellId, group.sourceKey) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SpellRow(
    spell: CharacterSpellUiModel,
    editMode: SheetEditMode,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Level ${spell.level} • ${spell.school}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (spell.castingTime.isNotBlank()) {
                    Text(
                        text = spell.castingTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (editMode == SheetEditMode.Editing) {
                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Remove spell")
                }
            }
        }
    }
}
