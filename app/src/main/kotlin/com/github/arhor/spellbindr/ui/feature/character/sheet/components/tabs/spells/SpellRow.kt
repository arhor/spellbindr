package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.components.SpellIcon
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
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
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                SpellIcon(
                    spellName = spell.name,
                    assetKey = spell.spellId,
                    size = 40.dp,
                    iconSize = 30.dp,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.titleMedium,
                )

                val detailText = buildList {
                    spell.school.takeIf { it.isNotBlank() }?.let(::add)
                    spell.castingTime.takeIf { it.isNotBlank() }?.let(::add)
                    spell.range.takeIf { it.isNotBlank() }?.let(::add)
                }.joinToString(separator = " · ")

                if (detailText.isNotEmpty()) {
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val components = spell.components
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .joinToString(separator = "")

                if (components.isNotEmpty() || spell.concentration || spell.ritual) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (components.isNotEmpty()) {
                            SpellTag(
                                text = components,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (spell.concentration) {
                            SpellTag(
                                text = stringResource(R.string.spells_concentration_chip),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        if (spell.ritual) {
                            SpellTag(
                                text = stringResource(R.string.spells_ritual_chip),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
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
                    FilledTonalButton(
                        onClick = onCastClick,
                        enabled = canCast,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
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
@PreviewLightDark
private fun SpellRowPreview() {
    AppTheme {
        SpellRow(
            spell = CharacterSpellUiModel(
                spellId = "fire_bolt",
                name = "Fire Bolt",
                level = 0,
                school = "Evocation",
                castingTime = "Action",
                range = "120 ft",
                components = listOf("V", "S"),
                ritual = false,
                concentration = true,
                sourceClass = "Wizard",
                sourceLabel = "Wizard",
                sourceKey = "wizard",
            ),
            editMode = SheetEditMode.View,
            onClick = {},
            canCast = true,
            onCastClick = {},
            onRemove = {},
        )
    }
}
