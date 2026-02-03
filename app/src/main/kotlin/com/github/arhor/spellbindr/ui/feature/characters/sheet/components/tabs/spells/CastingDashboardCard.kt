package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.ConcentrationUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSlotUiModel

@Composable
internal fun CastingDashboardCard(
    sharedSlots: List<SpellSlotUiModel>,
    hasConfiguredSharedSlots: Boolean,
    pactSlots: PactSlotUiModel?,
    concentration: ConcentrationUiModel?,
    editMode: SheetEditMode,
    onSharedSlotToggle: (Int, Int) -> Unit,
    onSharedSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
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
            concentration?.let {
                ConcentrationBanner(
                    concentration = it,
                    onClear = onConcentrationClear,
                )
            }
            SharedSlotsSection(
                sharedSlots = sharedSlots,
                hasConfiguredSharedSlots = hasConfiguredSharedSlots,
                editMode = editMode,
                onSlotToggle = onSharedSlotToggle,
                onSlotTotalChanged = onSharedSlotTotalChanged,
            )
            pactSlots?.let {
                PactSlotsSection(
                    pactSlots = it,
                    editMode = editMode,
                    onPactSlotToggle = onPactSlotToggle,
                    onPactSlotTotalChanged = onPactSlotTotalChanged,
                )
            }
        }
    }
}

@Composable
private fun SharedSlotsSection(
    sharedSlots: List<SpellSlotUiModel>,
    hasConfiguredSharedSlots: Boolean,
    editMode: SheetEditMode,
    onSlotToggle: (Int, Int) -> Unit,
    onSlotTotalChanged: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.spells_slots_title),
            style = MaterialTheme.typography.titleSmall,
        )
        val showEmptyState = !hasConfiguredSharedSlots
        if (sharedSlots.isEmpty() || (showEmptyState && editMode == SheetEditMode.View)) {
            Text(
                text = stringResource(R.string.spells_slots_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (sharedSlots.isEmpty() || editMode == SheetEditMode.View) {
                return
            }
        } else if (showEmptyState) {
            Text(
                text = stringResource(R.string.spells_slots_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            sharedSlots.forEach { slot ->
                SharedSlotItem(
                    slot = slot,
                    editMode = editMode,
                    onSlotToggle = onSlotToggle,
                    onSlotTotalChanged = onSlotTotalChanged,
                )
            }
        }
    }
}

@Composable
private fun SharedSlotItem(
    slot: SpellSlotUiModel,
    editMode: SheetEditMode,
    onSlotToggle: (Int, Int) -> Unit,
    onSlotTotalChanged: (Int, Int) -> Unit,
) {
    val available = (slot.total - slot.expended).coerceAtLeast(0)
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.spells_slot_level, slot.level),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.spells_slot_count, available, slot.total),
                style = MaterialTheme.typography.labelLarge,
            )
            SlotPipsRow(
                total = slot.total,
                expended = slot.expended,
                onPipClick = { index -> onSlotToggle(slot.level, index) },
                slotTypeLabel = stringResource(R.string.spells_slot_type_shared),
            )
            if (editMode == SheetEditMode.Edit) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onSlotTotalChanged(slot.level, slot.total - 1) },
                        enabled = slot.total > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = stringResource(
                                R.string.spells_shared_slots_decrease,
                                slot.level,
                            ),
                        )
                    }
                    IconButton(
                        onClick = { onSlotTotalChanged(slot.level, slot.total + 1) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(
                                R.string.spells_shared_slots_increase,
                                slot.level,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PactSlotsSection(
    pactSlots: PactSlotUiModel,
    editMode: SheetEditMode,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
) {
    val available = (pactSlots.total - pactSlots.expended).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.spells_pact_slots_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.spells_slot_count, available, pactSlots.total),
                    style = MaterialTheme.typography.labelLarge,
                )
                pactSlots.slotLevel?.let { level ->
                    Text(
                        text = stringResource(R.string.spells_level_label, level),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(R.string.spells_short_rest_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (editMode == SheetEditMode.Edit) {
                Row {
                    IconButton(
                        onClick = { onPactSlotTotalChanged(pactSlots.total - 1) },
                        enabled = pactSlots.total > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = stringResource(R.string.spells_pact_slots_decrease),
                        )
                    }
                    IconButton(
                        onClick = { onPactSlotTotalChanged(pactSlots.total + 1) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.spells_pact_slots_increase),
                        )
                    }
                }
            }
        }
        if (!pactSlots.isConfigured && pactSlots.total == 0) {
            Text(
                text = stringResource(R.string.spells_not_configured),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SlotPipsRow(
                total = pactSlots.total,
                expended = pactSlots.expended,
                onPipClick = onPactSlotToggle,
                slotTypeLabel = stringResource(R.string.spells_slot_type_pact),
            )
        }
    }
}

@Composable
private fun SlotPipsRow(
    total: Int,
    expended: Int,
    onPipClick: (Int) -> Unit,
    slotTypeLabel: String,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) {
        Spacer(modifier = modifier.height(4.dp))
        return
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            val used = index < expended
            val contentDescription = if (used) {
                stringResource(R.string.spells_slot_pip_used, slotTypeLabel, index + 1, total)
            } else {
                stringResource(R.string.spells_slot_pip_available, slotTypeLabel, index + 1, total)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .semantics { this.contentDescription = contentDescription }
                    .clickable { onPipClick(index) },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (used) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = if (used) 2.dp else 0.dp,
                ) {
                    Spacer(modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ConcentrationBanner(
    concentration: ConcentrationUiModel,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.spells_concentrating, concentration.label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onClear) {
                Text(text = stringResource(R.string.spells_clear))
            }
        }
    }
}
