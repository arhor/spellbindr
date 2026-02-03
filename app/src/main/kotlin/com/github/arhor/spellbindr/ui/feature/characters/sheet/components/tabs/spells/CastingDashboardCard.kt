package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.ConcentrationUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSlotUiModel

@Composable
internal fun CastingDashboardCard(
    sharedSlots: List<SpellSlotUiModel>,
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
    editMode: SheetEditMode,
    onSlotToggle: (Int, Int) -> Unit,
    onSlotTotalChanged: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Spell slots",
            style = MaterialTheme.typography.titleSmall,
        )
        if (sharedSlots.isEmpty()) {
            Text(
                text = "No spell slots configured.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
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
                text = "L${slot.level}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$available/${slot.total}",
                style = MaterialTheme.typography.labelLarge,
            )
            SlotPipsRow(
                total = slot.total,
                expended = slot.expended,
                onPipClick = { index -> onSlotToggle(slot.level, index) },
            )
            if (editMode == SheetEditMode.Edit) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onSlotTotalChanged(slot.level, slot.total - 1) },
                        enabled = slot.total > 0,
                    ) {
                        Text(text = "-")
                    }
                    IconButton(
                        onClick = { onSlotTotalChanged(slot.level, slot.total + 1) },
                    ) {
                        Text(text = "+")
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
                    text = "Pact slots",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "$available/${pactSlots.total}",
                    style = MaterialTheme.typography.labelLarge,
                )
                pactSlots.slotLevel?.let { level ->
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Short Rest",
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
                        Text(text = "-")
                    }
                    IconButton(
                        onClick = { onPactSlotTotalChanged(pactSlots.total + 1) },
                    ) {
                        Text(text = "+")
                    }
                }
            }
        }
        if (!pactSlots.isConfigured && pactSlots.total == 0) {
            Text(
                text = "Not configured.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SlotPipsRow(
                total = pactSlots.total,
                expended = pactSlots.expended,
                onPipClick = onPactSlotToggle,
            )
        }
    }
}

@Composable
private fun SlotPipsRow(
    total: Int,
    expended: Int,
    onPipClick: (Int) -> Unit,
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
            Surface(
                shape = CircleShape,
                color = if (used) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (used) 2.dp else 0.dp,
                onClick = { onPipClick(index) },
            ) {
                Spacer(modifier = Modifier.size(14.dp))
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
                text = "Concentrating: ${concentration.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onClear) {
                Text(text = "Clear")
            }
        }
    }
}
