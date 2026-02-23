package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.feature.character.R
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.signed

@Composable
internal fun SpellcastingClassCard(
    spellcastingClass: SpellcastingClassUiModel,
    sharedSlots: List<SpellSlotUiModel>,
    pactSlots: PactSlotUiModel?,
    showSharedSlotBadges: Boolean,
    editMode: SheetEditMode,
    canCastFor: (CharacterSpellUiModel) -> Boolean,
    onCastSpellClick: (String) -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = classAccentColorFor(spellcastingClass.sourceKey)
    val icon = classIconFor(spellcastingClass.sourceKey)

    val unknown = stringResource(R.string.spells_unknown_placeholder)
    val title = if (spellcastingClass.isUnassigned) {
        stringResource(R.string.spells_unassigned)
    } else {
        spellcastingClass.name.ifBlank { stringResource(R.string.spells_unassigned) }
    }
    val abilityText = spellcastingClass.spellcastingAbility ?: unknown
    val dcText = spellcastingClass.spellSaveDc?.toString() ?: unknown
    val attackText = spellcastingClass.spellAttackBonus?.let(::signed) ?: unknown

    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "$abilityText | DC $dcText | $attackText",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(min = 84.dp),
                    )
                }

                if (editMode == SheetEditMode.View) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (showSharedSlotBadges) {
                            val sharedBadges = remember(sharedSlots) {
                                sharedSlots
                                    .sortedBy(SpellSlotUiModel::level)
                                    .filter { it.total > 0 }
                                    .take(2)
                            }
                            sharedBadges.forEach { slot ->
                                val available = (slot.total - slot.expended).coerceAtLeast(0)
                                SlotBadge(
                                    text = "${slot.level.toOrdinalLabel()} Spell Slot ${available}/${slot.total}",
                                )
                            }
                        }
                        if (spellcastingClass.sourceKey.lowercase() == "warlock" && pactSlots != null) {
                            val label = pactSlots.slotLevel?.let { level ->
                                "(${level.toOrdinalLabel()}) "
                            }.orEmpty()
                            val available = (pactSlots.total - pactSlots.expended).coerceAtLeast(0)
                            SlotBadge(
                                text = "Pact Slots $label${available}/${pactSlots.total}".trim(),
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    spellcastingClass.spellLevels.forEach { level ->
                        SpellLevelSection(
                            level = level.level,
                            spells = level.spells,
                            editMode = editMode,
                            canCastFor = canCastFor,
                            onCastSpellClick = onCastSpellClick,
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
private fun SpellLevelSection(
    level: Int,
    spells: List<CharacterSpellUiModel>,
    editMode: SheetEditMode,
    canCastFor: (CharacterSpellUiModel) -> Boolean,
    onCastSpellClick: (String) -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val label = if (level == 0) {
                "${stringResource(R.string.spells_level_cantrips)} (${spells.size})"
            } else {
                "${level.toOrdinalLabel()} Level (${spells.size})"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            spells.forEach { spell ->
                SpellRow(
                    spell = spell,
                    editMode = editMode,
                    onClick = { onSpellSelected(spell.spellId) },
                    canCast = canCastFor(spell),
                    onCastClick = { onCastSpellClick(spell.spellId) },
                    onRemove = { onSpellRemoved(spell.spellId, spell.sourceClass) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SlotBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Preview
@Composable
private fun SpellcastingClassCardPreview() {
    AppTheme {
        SpellcastingClassCard(
            spellcastingClass = CharacterSheetPreviewData.spells.spellcastingClasses.first(),
            sharedSlots = CharacterSheetPreviewData.spells.sharedSlots,
            pactSlots = CharacterSheetPreviewData.spells.pactSlots,
            showSharedSlotBadges = true,
            editMode = SheetEditMode.View,
            canCastFor = { true },
            onCastSpellClick = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
