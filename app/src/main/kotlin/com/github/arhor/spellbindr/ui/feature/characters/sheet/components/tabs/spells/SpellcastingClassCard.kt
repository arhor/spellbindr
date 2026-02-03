package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellcastingClassCard(
    spellcastingClass: SpellcastingClassUiModel,
    editMode: SheetEditMode,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = spellcastingClass.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SpellcastingStat(text = "Ability ${spellcastingClass.spellcastingAbilityLabel}")
                    SpellcastingStat(text = spellcastingClass.spellSaveDcLabel)
                    SpellcastingStat(text = spellcastingClass.spellAttackBonusLabel)
                }
            }

            if (spellcastingClass.spellLevels.isEmpty()) {
                Text(
                    text = "No spells for this class yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    spellcastingClass.spellLevels.forEach { level ->
                        SpellLevelSection(
                            spellLevel = level,
                            editMode = editMode,
                            onSpellSelected = onSpellSelected,
                            onSpellRemoved = onSpellRemoved,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellcastingStat(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun SpellLevelSection(
    spellLevel: SpellLevelUiModel,
    editMode: SheetEditMode,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = spellLevel.label,
            style = MaterialTheme.typography.titleSmall,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            spellLevel.spells.forEach { spell ->
                SpellRow(
                    spell = spell,
                    editMode = editMode,
                    onClick = { onSpellSelected(spell.spellId) },
                    onRemove = { onSpellRemoved(spell.spellId, spell.sourceClass) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun SpellcastingClassCardPreview() {
    AppTheme {
        SpellcastingClassCard(
            spellcastingClass = CharacterSheetPreviewData.spells.spellcastingClasses.first(),
            editMode = SheetEditMode.View,
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
        )
    }
}

