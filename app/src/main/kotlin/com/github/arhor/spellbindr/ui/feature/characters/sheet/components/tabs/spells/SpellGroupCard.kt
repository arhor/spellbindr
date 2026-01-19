package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

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
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellLevelCard(
    spellLevel: SpellLevelUiModel,
    editMode: SheetEditMode,
    showSourceBadges: Boolean,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
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
                            showSourceBadge = showSourceBadges,
                            onClick = { onSpellSelected(spell.spellId) },
                            onRemove = { onSpellRemoved(spell.spellId, spell.sourceClass) },
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
    showSourceBadge: Boolean,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    if (!showSourceBadge) {
                        spell.sourceLabel.takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                }
                if (detailParts.isNotEmpty()) {
                    Text(
                        text = detailParts.joinToString(separator = " • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (showSourceBadge) {
                SpellSourceBadge(label = spell.sourceLabel)
            }
            if (editMode == SheetEditMode.Edit) {
                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Remove spell")
                }
            }
        }
    }
}

@Composable
private fun SpellSourceBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Preview
@Composable
private fun SpellLevelCardPreview() {
    AppTheme {
        SpellLevelCard(
            spellLevel = CharacterSheetPreviewData.spells.spellLevels.first(),
            editMode = SheetEditMode.View,
            showSourceBadges = CharacterSheetPreviewData.spells.showSourceBadges,
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun SpellRowPreview() {
    AppTheme {
        SpellRow(
            spell = CharacterSheetPreviewData.spells.spellLevels.first { it.spells.isNotEmpty() }.spells.first(),
            editMode = SheetEditMode.Edit,
            showSourceBadge = CharacterSheetPreviewData.spells.showSourceBadges,
            onClick = {},
            onRemove = {},
        )
    }
}
