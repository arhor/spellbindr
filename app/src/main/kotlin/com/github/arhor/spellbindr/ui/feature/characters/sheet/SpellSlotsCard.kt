package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellSlotsCard(
    slots: List<SpellSlotUiModel>,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
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

@Preview
@Composable
private fun SpellSlotsCardPreview() {
    AppTheme {
        SpellSlotsCard(
            slots = CharacterSheetPreviewData.spells.spellSlots,
            editMode = SheetEditMode.View,
            callbacks = CharacterSheetCallbacks(),
        )
    }
}

@Preview
@Composable
private fun SpellSlotsCardEditingPreview() {
    AppTheme {
        SpellSlotsCard(
            slots = CharacterSheetPreviewData.spells.spellSlots,
            editMode = SheetEditMode.Editing,
            callbacks = CharacterSheetCallbacks(),
        )
    }
}
