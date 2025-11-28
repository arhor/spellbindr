package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

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
import androidx.compose.material.icons.rounded.Close
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
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellLevelCard(
    spellLevel: SpellLevelUiModel,
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
                text = spellLevel.label,
                style = MaterialTheme.typography.titleMedium,
            )

            spellLevel.spellSlot?.let { slot ->
                Spacer(modifier = Modifier.height(12.dp))
                SpellSlotSection(
                    slot = slot,
                    editMode = editMode,
                    callbacks = callbacks,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (spellLevel.spells.isEmpty()) {
                Text(
                    text = "No spells for this level yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    spellLevel.spells.forEach { spell ->
                        SpellRow(
                            spell = spell,
                            editMode = editMode,
                            onClick = { callbacks.onSpellSelected(spell.spellId) },
                            onRemove = { callbacks.onSpellRemoved(spell.spellId, spell.sourceClass) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SpellRow(
    spell: CharacterSpellUiModel,
    editMode: SheetEditMode,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
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
                val detailParts = buildList {
                    add(spell.school)
                    spell.castingTime.takeIf { it.isNotBlank() }?.let { add(it) }
                    spell.sourceClass.takeIf { it.isNotBlank() }?.let { add(it) }
                }
                if (detailParts.isNotEmpty()) {
                    Text(
                        text = detailParts.joinToString(separator = " • "),
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

@Composable
private fun SpellSlotSection(
    slot: SpellSlotUiModel,
    editMode: SheetEditMode,
    callbacks: CharacterSheetCallbacks,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Spell slots",
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

        if (slot.total == 0) {
            Text(
                text = "No slots configured for this level",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
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
private fun SpellLevelCardPreview() {
    AppTheme {
        SpellLevelCard(
            spellLevel = CharacterSheetPreviewData.spells.spellLevels.first(),
            editMode = SheetEditMode.View,
            callbacks = CharacterSheetCallbacks(),
        )
    }
}

@Preview
@Composable
private fun SpellLevelCardWithSlotsPreview() {
    AppTheme {
        SpellLevelCard(
            spellLevel = CharacterSheetPreviewData.spells.spellLevels.first { it.level == 1 },
            editMode = SheetEditMode.Editing,
            callbacks = CharacterSheetCallbacks(),
        )
    }
}

@Preview
@Composable
private fun SpellRowPreview() {
    AppTheme {
        SpellRow(
            spell = CharacterSheetPreviewData.spells.spellLevels.first { it.spells.isNotEmpty() }.spells.first(),
            editMode = SheetEditMode.Editing,
            onClick = {},
            onRemove = {},
        )
    }
}
