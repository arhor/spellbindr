package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SpellRow(
    spell: CharacterSpellUiModel,
    editMode: SheetEditMode,
    onClick: () -> Unit,
    canCast: Boolean,
    onCastClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.bodyLarge,
                )

                val detailParts = buildList {
                    spell.school.takeIf { it.isNotBlank() }?.let { add(it) }
                    spell.castingTime.takeIf { it.isNotBlank() }?.let { add(it) }
                    spell.range.takeIf { it.isNotBlank() }?.let { add(it) }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    detailParts.forEach { part ->
                        Text(
                            text = part,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val components = spell.components
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString(separator = "")

                    if (components.isNotEmpty()) {
                        SpellTag(
                            text = components,
                            containerColor = MaterialTheme.colorScheme.outline,
                            contentColor = MaterialTheme.colorScheme.surfaceBright,
                        )
                    }
                    if (spell.concentration) {
                        SpellTag(
                            text = stringResource(R.string.spells_concentration_chip),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (spell.ritual) {
                        SpellTag(
                            text = stringResource(R.string.spells_ritual_chip),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            when (editMode) {
                SheetEditMode.Edit -> {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.spells_remove_spell),
                        )
                    }
                }

                SheetEditMode.View -> {
                    Button(
                        onClick = onCastClick,
                        enabled = canCast,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline,
                            contentColor = MaterialTheme.colorScheme.surfaceBright,
                            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            text = stringResource(R.string.spells_cast_action),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellTag(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
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
            editMode = SheetEditMode.View,
            onClick = {},
            canCast = true,
            onCastClick = {},
            onRemove = {},
        )
    }
}
