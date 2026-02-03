package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

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
                    spell.school.takeIf { it.isNotBlank() }?.let { add(it) }
                    spell.castingTime.takeIf { it.isNotBlank() }?.let { add(it) }
                }
                if (detailParts.isNotEmpty()) {
                    Text(
                        text = detailParts.joinToString(separator = " • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (editMode == SheetEditMode.Edit) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.spells_remove_spell),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun SpellRowPreview() {
    AppTheme {
        SpellRow(
            spell = CharacterSheetPreviewData.spells
                .spellcastingClasses
                .first()
                .spellLevels
                .first()
                .spells
                .first(),
            editMode = SheetEditMode.Edit,
            onClick = {},
            onRemove = {},
        )
    }
}
