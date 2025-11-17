package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.AbilityTokenData
import com.github.arhor.spellbindr.ui.components.AbilityTokensGrid
import com.github.arhor.spellbindr.ui.components.D20HpBar

@Composable
fun OverviewTab(
    header: CharacterHeaderUiState,
    overview: OverviewTabState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    var showHpAdjustDialog by remember { mutableStateOf(false) }
    val onHitPointsClick = if (editMode == SheetEditMode.View) {
        { showHpAdjustDialog = true }
    } else {
        null
    }

    if (showHpAdjustDialog) {
        HitPointAdjustDialog(
            hitPoints = header.hitPoints,
            onAdjustHp = callbacks.onAdjustHp,
            onTempHpChanged = callbacks.onTempHpChanged,
            onDismiss = { showHpAdjustDialog = false },
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val isAtOrBelowZeroHp = header.hitPoints.current <= 0
            if (isAtOrBelowZeroHp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CombatOverviewCard(
                        header = header,
                        editMode = editMode,
                        editingState = editingState,
                        callbacks = callbacks,
                        onHitPointsClick = onHitPointsClick,
                        modifier = Modifier.weight(1f),
                    )
                    DeathSavesCard(
                        state = overview.deathSaves,
                        onSuccessChanged = callbacks.onDeathSaveSuccessesChanged,
                        onFailureChanged = callbacks.onDeathSaveFailuresChanged,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                CombatOverviewCard(
                    header = header,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                    onHitPointsClick = onHitPointsClick,
                )
            }
        }
        item {
            AbilityTokensGrid(
                abilities = overview.abilities.map { ability ->
                    AbilityTokenData(
                        abbreviation = ability.label,
                        score = ability.score,
                        modifier = ability.modifier,
                        savingThrowBonus = ability.savingThrowBonus,
                        proficient = ability.savingThrowProficient,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            DetailCard(
                title = "Core details",
                lines = listOfNotBlank(
                    overview.race.takeIf { it.isNotBlank() }?.let { "Race: $it" },
                    overview.background.takeIf { it.isNotBlank() }?.let { "Background: $it" },
                    overview.alignment.takeIf { it.isNotBlank() }?.let { "Alignment: $it" },
                ),
            )
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Senses & Languages",
                    primaryLabel = "Senses",
                    primaryValue = editingState.senses,
                    onPrimaryChanged = callbacks.onSensesEdited,
                    secondaryLabel = "Languages",
                    secondaryValue = editingState.languages,
                    onSecondaryChanged = callbacks.onLanguagesEdited,
                )
            } else {
                DetailCard(
                    title = "Senses & Languages",
                    lines = listOfNotBlank(
                        overview.senses.takeIf { it.isNotBlank() }?.let { "Senses: $it" },
                        overview.languages.takeIf { it.isNotBlank() }?.let { "Languages: $it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Proficiencies & Equipment",
                    primaryLabel = "Proficiencies",
                    primaryValue = editingState.proficiencies,
                    onPrimaryChanged = callbacks.onProficienciesEdited,
                    secondaryLabel = "Equipment",
                    secondaryValue = editingState.equipment,
                    onSecondaryChanged = callbacks.onEquipmentEdited,
                )
            } else {
                DetailCard(
                    title = "Proficiencies & Equipment",
                    lines = listOfNotBlank(
                        overview.proficiencies.takeIf { it.isNotBlank() }?.let { "Proficiencies:\n$it" },
                        overview.equipment.takeIf { it.isNotBlank() }?.let { "Equipment:\n$it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                InlineTextField(
                    label = "Hit Dice",
                    value = editingState.hitDice,
                    onValueChanged = callbacks.onHitDiceEdited,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DetailCard(
                    title = "Hit Dice",
                    lines = listOf(overview.hitDice.ifBlank { "—" }),
                )
            }
        }
    }
}

@Composable
private fun HitPointAdjustDialog(
    hitPoints: HitPointSummary,
    onAdjustHp: (Int) -> Unit,
    onTempHpChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = { Text("Adjust hit points") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${hitPoints.current} / ${hitPoints.max}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Column {
                    Text(
                        text = "Modify HP",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(-5, -1, 1, 5).forEach { delta ->
                            val label = if (delta > 0) "+$delta" else delta.toString()
                            HpAdjustButton(
                                label = label,
                                onClick = { onAdjustHp(delta) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Column {
                    Text(
                        text = "Temporary HP ${hitPoints.temporary}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AssistChip(
                            onClick = { onTempHpChanged((hitPoints.temporary - 1).coerceAtLeast(0)) },
                            label = { Text("-1") },
                            enabled = hitPoints.temporary > 0,
                        )
                        AssistChip(
                            onClick = { onTempHpChanged(hitPoints.temporary + 1) },
                            label = { Text("+1") },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun CombatOverviewCard(
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
            Column(modifier = Modifier.padding(16.dp)) {
                HitPointBlock(
                    hitPoints = header.hitPoints,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                    onHitPointsClick = onHitPointsClick,
                )
                Spacer(modifier = Modifier.height(20.dp))
                StatsRow(
                    header = header,
                    editMode = editMode,
                    editingState = editingState,
                    callbacks = callbacks,
                )
            }
        } else {
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
                    HitPointBlock(
                        hitPoints = header.hitPoints,
                        editMode = editMode,
                        editingState = editingState,
                        callbacks = callbacks,
                        onHitPointsClick = onHitPointsClick,
                    )
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
    }
}

@Composable
private fun HpAdjustButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 12.dp,
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            end = 12.dp,
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
        ),
    ) {
        Text(label)
    }
}

@Composable
private fun DeathSavesCard(
    state: DeathSaveUiState,
    onSuccessChanged: (Int) -> Unit,
    onFailureChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Death Saves",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            DeathSaveTrack(
                label = "Successes",
                count = state.successes,
                color = MaterialTheme.colorScheme.primary,
                onCountChanged = onSuccessChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))
            DeathSaveTrack(
                label = "Failures",
                count = state.failures,
                color = MaterialTheme.colorScheme.error,
                onCountChanged = onFailureChanged,
            )
        }
    }
}

@Composable
private fun DeathSaveTrack(
    label: String,
    count: Int,
    color: Color,
    onCountChanged: (Int) -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                val isFilled = index < count
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (isFilled) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isFilled) color else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        )
                        .padding(6.dp)
                        .clickable { onCountChanged(if (isFilled) index else index + 1) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isFilled) {
                        Icon(
                            imageVector = if (label == "Failures") Icons.Rounded.Close else Icons.Rounded.Check,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    lines: List<String>,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (lines.isEmpty()) "No information yet" else lines.joinToString("\n\n"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditableDetailCard(
    title: String,
    primaryLabel: String,
    primaryValue: String,
    onPrimaryChanged: (String) -> Unit,
    secondaryLabel: String,
    secondaryValue: String,
    onSecondaryChanged: (String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = primaryLabel,
                value = primaryValue,
                onValueChanged = onPrimaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            InlineTextField(
                label = secondaryLabel,
                value = secondaryValue,
                onValueChanged = onSecondaryChanged,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CombatStatRow(
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
                textAlign = TextAlign.End,
            )
        }
    }
}

private fun listOfNotBlank(vararg values: String?): List<String> =
    values.mapNotNull { value -> value?.takeIf { it.isNotBlank() }?.trim() }

@Composable
private fun HitPointBlock(
    hitPoints: HitPointSummary,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    onHitPointsClick: (() -> Unit)?,
) {
    when {
        editMode == SheetEditMode.Editing && editingState != null -> {
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
        }

        else -> {
            val hpModifier = if (onHitPointsClick != null) {
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = onHitPointsClick)
            } else {
                Modifier.fillMaxWidth()
            }
            Column(
                modifier = hpModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                D20HpBar(
                    currentHp = hitPoints.current,
                    maxHp = hitPoints.max,
                )

                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    header: CharacterHeaderUiState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatChip(label = "AC", value = header.armorClass.toString(), modifier = Modifier.weight(1f))
        StatChip(label = "Initiative", value = formatBonus(header.initiative), modifier = Modifier.weight(1f))
        if (editMode == SheetEditMode.Editing && editingState != null) {
            InlineTextField(
                label = "Speed",
                value = editingState.speed,
                onValueChanged = callbacks.onSpeedEdited,
                modifier = Modifier.weight(1f),
            )
        } else {
            StatChip(label = "Speed", value = header.speed, modifier = Modifier.weight(1f))
        }
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
