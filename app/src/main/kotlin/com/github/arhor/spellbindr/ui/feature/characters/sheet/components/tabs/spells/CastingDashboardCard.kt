package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
    onLongRestClick: () -> Unit,
    onShortRestClick: () -> Unit,
    onConfigureSlotsClick: () -> Unit,
    onSharedSlotToggle: (Int, Int) -> Unit,
    onSharedSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onPactSlotLevelChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canLongRest = remember(sharedSlots, pactSlots) {
        sharedSlots.any { it.expended > 0 } || (pactSlots?.expended ?: 0) > 0
    }
    val canShortRest = remember(pactSlots) {
        pactSlots != null && pactSlots.expended > 0
    }

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

            SlotsHeader(
                hasPactSlots = pactSlots != null,
                canLongRest = canLongRest,
                canShortRest = canShortRest,
                onLongRestClick = onLongRestClick,
                onShortRestClick = onShortRestClick,
            )

            SharedSlotsSection(
                sharedSlots = sharedSlots,
                hasConfiguredSharedSlots = hasConfiguredSharedSlots,
                editMode = editMode,
                onConfigureSlotsClick = onConfigureSlotsClick,
                onSlotToggle = onSharedSlotToggle,
                onSlotTotalChanged = onSharedSlotTotalChanged,
            )

            pactSlots?.let {
                PactSlotsSection(
                    pactSlots = it,
                    editMode = editMode,
                    onPactSlotToggle = onPactSlotToggle,
                    onPactSlotTotalChanged = onPactSlotTotalChanged,
                    onPactSlotLevelChanged = onPactSlotLevelChanged,
                )
            }
        }
    }
}

@Composable
private fun SlotsHeader(
    hasPactSlots: Boolean,
    canLongRest: Boolean,
    canShortRest: Boolean,
    onLongRestClick: () -> Unit,
    onShortRestClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.spells_slots_title),
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onLongRestClick,
                enabled = canLongRest,
            ) {
                Text(text = stringResource(R.string.spells_long_rest))
            }
            if (hasPactSlots) {
                TextButton(
                    onClick = onShortRestClick,
                    enabled = canShortRest,
                ) {
                    Text(text = stringResource(R.string.spells_short_rest_label))
                }
            }
        }
    }
}

@Composable
private fun SharedSlotsSection(
    sharedSlots: List<SpellSlotUiModel>,
    hasConfiguredSharedSlots: Boolean,
    editMode: SheetEditMode,
    onConfigureSlotsClick: () -> Unit,
    onSlotToggle: (Int, Int) -> Unit,
    onSlotTotalChanged: (Int, Int) -> Unit,
) {
    val showEmptyState = !hasConfiguredSharedSlots
    val slotsEnabled = !(showEmptyState && editMode == SheetEditMode.View)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showEmptyState) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.spells_slots_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (editMode == SheetEditMode.View) {
                    TextButton(onClick = onConfigureSlotsClick) {
                        Text(text = stringResource(R.string.spells_configure_slots))
                    }
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            sharedSlots.forEach { slot ->
                SharedSlotTile(
                    slot = slot,
                    editMode = editMode,
                    enabled = slotsEnabled,
                    onSlotToggle = onSlotToggle,
                    onSlotTotalChanged = onSlotTotalChanged,
                )
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
    onPactSlotLevelChanged: (Int) -> Unit,
) {
    val available = (pactSlots.total - pactSlots.expended).coerceAtLeast(0)
    val canSpend = pactSlots.total > 0 && available > 0
    val canRestore = pactSlots.total > 0 && pactSlots.expended > 0

    fun spendOne() {
        if (canSpend) onPactSlotToggle(pactSlots.expended)
    }

    fun restoreOne() {
        if (canRestore) onPactSlotToggle(pactSlots.expended - 1)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.spells_pact_slots_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }
                Text(
                    text = stringResource(R.string.spells_slot_count, available, pactSlots.total),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            if (!pactSlots.isConfigured && pactSlots.total == 0 && editMode == SheetEditMode.View) {
                Text(
                    text = stringResource(R.string.spells_not_configured),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SlotPipsRow(
                    total = pactSlots.total,
                    expended = pactSlots.expended,
                    enabled = pactSlots.total > 0,
                    onSpendOne = ::spendOne,
                    onRestoreOne = ::restoreOne,
                    slotTypeLabel = stringResource(R.string.spells_slot_type_pact),
                )
            }

            when (editMode) {
                SheetEditMode.View -> {
                    if (pactSlots.total > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = ::spendOne,
                                enabled = canSpend,
                                label = { Text(text = stringResource(R.string.spells_use_slot)) },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Remove, contentDescription = null) },
                            )
                            AssistChip(
                                onClick = ::restoreOne,
                                enabled = canRestore,
                                label = { Text(text = stringResource(R.string.spells_restore_slot)) },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                            )
                        }
                    }
                }

                SheetEditMode.Edit -> {
                    val displayedLevel = pactSlots.slotLevel ?: 1
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepperRow(
                            label = stringResource(R.string.spells_total_label),
                            value = pactSlots.total,
                            decrementContentDescription = stringResource(R.string.spells_pact_slots_decrease),
                            incrementContentDescription = stringResource(R.string.spells_pact_slots_increase),
                            onDecrement = { onPactSlotTotalChanged(pactSlots.total - 1) },
                            onIncrement = { onPactSlotTotalChanged(pactSlots.total + 1) },
                            canDecrement = pactSlots.total > 0,
                        )

                        StepperRow(
                            label = stringResource(R.string.spells_pact_level_label),
                            value = displayedLevel,
                            decrementContentDescription = stringResource(R.string.spells_pact_level_decrease),
                            incrementContentDescription = stringResource(R.string.spells_pact_level_increase),
                            onDecrement = { onPactSlotLevelChanged((displayedLevel - 1).coerceAtLeast(1)) },
                            onIncrement = { onPactSlotLevelChanged((displayedLevel + 1).coerceAtMost(9)) },
                            canDecrement = displayedLevel > 1,
                            canIncrement = displayedLevel < 9,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotPipsRow(
    total: Int,
    expended: Int,
    enabled: Boolean,
    onSpendOne: () -> Unit,
    onRestoreOne: () -> Unit,
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
            val isExpended = index < expended
            val contentDescription = if (isExpended) {
                stringResource(R.string.spells_slot_pip_used, slotTypeLabel, index + 1, total)
            } else {
                stringResource(R.string.spells_slot_pip_available, slotTypeLabel, index + 1, total)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isExpended) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isExpended) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    )
                    .semantics { this.contentDescription = contentDescription }
                    .clickable(enabled = enabled) { if (isExpended) onRestoreOne() else onSpendOne() },
                contentAlignment = Alignment.Center,
            ) {
                Spacer(modifier = Modifier.size(1.dp))
            }
        }
    }
}

@Composable
private fun SharedSlotTile(
    slot: SpellSlotUiModel,
    editMode: SheetEditMode,
    enabled: Boolean,
    onSlotToggle: (Int, Int) -> Unit,
    onSlotTotalChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = (slot.total - slot.expended).coerceAtLeast(0)
    val canSpend = enabled && slot.total > 0 && available > 0
    val canRestore = enabled && slot.total > 0 && slot.expended > 0

    fun spendOne() {
        if (canSpend) onSlotToggle(slot.level, slot.expended)
    }

    fun restoreOne() {
        if (canRestore) onSlotToggle(slot.level, slot.expended - 1)
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.widthIn(min = 156.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.spells_level_label, slot.level),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.spells_slot_count, available, slot.total),
                style = MaterialTheme.typography.titleSmall,
            )
            SlotPipsRow(
                total = slot.total,
                expended = slot.expended,
                enabled = enabled && slot.total > 0,
                onSpendOne = ::spendOne,
                onRestoreOne = ::restoreOne,
                slotTypeLabel = stringResource(R.string.spells_slot_type_shared),
            )

            when (editMode) {
                SheetEditMode.View -> {
                    if (slot.total > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = ::spendOne,
                                enabled = canSpend,
                                label = { Text(text = stringResource(R.string.spells_use_slot)) },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Remove, contentDescription = null) },
                            )
                            AssistChip(
                                onClick = ::restoreOne,
                                enabled = canRestore,
                                label = { Text(text = stringResource(R.string.spells_restore_slot)) },
                                leadingIcon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                            )
                        }
                    }
                }

                SheetEditMode.Edit -> {
                    StepperRow(
                        label = stringResource(R.string.spells_total_label),
                        value = slot.total,
                        decrementContentDescription = stringResource(
                            R.string.spells_shared_slots_decrease,
                            slot.level,
                        ),
                        incrementContentDescription = stringResource(
                            R.string.spells_shared_slots_increase,
                            slot.level,
                        ),
                        onDecrement = { onSlotTotalChanged(slot.level, slot.total - 1) },
                        onIncrement = { onSlotTotalChanged(slot.level, slot.total + 1) },
                        canDecrement = slot.total > 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    decrementContentDescription: String,
    incrementContentDescription: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    canDecrement: Boolean,
    canIncrement: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(
            onClick = onDecrement,
            enabled = canDecrement,
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = decrementContentDescription,
            )
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 20.dp),
        )
        IconButton(
            onClick = onIncrement,
            enabled = canIncrement,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = incrementContentDescription,
            )
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
