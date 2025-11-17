package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.D20HpBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun CombatOverviewCard(
    header: CharacterHeaderUiState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    onHitPointsClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (editMode == SheetEditMode.Editing && editingState != null) {
            ContentEdit(header, editingState, callbacks)
        } else {
            ContentView(header, onHitPointsClick)
        }
    }
}

@Preview
@Composable
private fun CombatOverviewCardPreview() {
    AppTheme {
        CombatOverviewCard(
            header = CharacterSheetPreviewData.header,
            editMode = SheetEditMode.View,
            editingState = null,
            callbacks = CharacterSheetCallbacks(),
            onHitPointsClick = {},
        )
    }
}

@Preview
@Composable
private fun CombatOverviewCardEditingPreview() {
    AppTheme {
        CombatOverviewCard(
            header = CharacterSheetPreviewData.header,
            editMode = SheetEditMode.Editing,
            editingState = CharacterSheetPreviewData.editingState,
            callbacks = CharacterSheetCallbacks(),
            onHitPointsClick = {},
        )
    }
}

@Composable
private fun ContentEdit(
    header: CharacterHeaderUiState,
    editingState: CharacterSheetEditingState,
    callbacks: CharacterSheetCallbacks,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(
                text = "Hit Points",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InlineNumberField(
                    label = "Current",
                    value = editingState.currentHp,
                    onValueChanged = callbacks.onCurrentHpEdited,
                    modifier = Modifier.weight(1f),
                )
                InlineNumberField(
                    label = "Max",
                    value = editingState.maxHp,
                    onValueChanged = callbacks.onMaxHpEdited,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            InlineNumberField(
                label = "Temporary",
                value = editingState.tempHp,
                onValueChanged = callbacks.onTempHpEdited,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        StatsRow(header = header)
    }
}

@Composable
private fun ContentView(
    header: CharacterHeaderUiState,
    onHitPointsClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val hpModifier = if (onHitPointsClick != null) {
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHitPointsClick)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(
                modifier = hpModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                D20HpBar(
                    currentHp = header.hitPoints.current,
                    maxHp = header.hitPoints.max,
                )

                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CombatStatRow(
                label = "AC",
                value = header.armorClass.toString(),
            )
            CombatStatRow(
                label = "Initiative",
                value = formatBonus(header.initiative),
            )
            CombatStatRow(
                label = "Speed",
                value = header.speed,
            )
        }
    }
}

@Composable
internal fun CombatStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun StatsRow(
    header: CharacterHeaderUiState,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(label = "AC", value = header.armorClass.toString(), modifier = Modifier.weight(1f))
        StatChip(label = "Initiative", value = formatBonus(header.initiative), modifier = Modifier.weight(1f))
        StatChip(label = "Speed", value = header.speed, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
