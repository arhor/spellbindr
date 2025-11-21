package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellcastingGroupUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellGroupCard(
    group: SpellcastingGroupUiModel,
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

@Preview
@Composable
private fun SpellGroupCardPreview() {
    AppTheme {
        SpellGroupCard(
            group = CharacterSheetPreviewData.spells.spellcastingGroups.first(),
            editMode = SheetEditMode.View,
            callbacks = CharacterSheetCallbacks(),
        )
    }
}

@Preview
@Composable
private fun SpellRowPreview() {
    AppTheme {
        SpellRow(
            spell = CharacterSheetPreviewData.spells.spellcastingGroups.first().spells.first(),
            editMode = SheetEditMode.Editing,
            onClick = {},
            onRemove = {},
        )
    }
}
