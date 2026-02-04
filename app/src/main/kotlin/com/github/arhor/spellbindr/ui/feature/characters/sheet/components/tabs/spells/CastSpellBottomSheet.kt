package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CastSlotOptionUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellCastUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSlotPool

@Composable
internal fun CastSpellBottomSheetContent(
    castSpell: SpellCastUiModel,
    onCancel: () -> Unit,
    onCast: (pool: SpellSlotPool?, slotLevel: Int?, castAsRitual: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCantrip = castSpell.level <= 0
    val showRitualToggle = castSpell.isRitual && castSpell.level > 0
    var castAsRitual by remember(castSpell.spellId) { mutableStateOf(false) }

    val slotOptions = remember(castSpell.slotOptions) {
        buildList {
            addAll(castSpell.slotOptions.filter { it.pool == SpellSlotPool.Shared }.sortedBy { it.slotLevel })
            addAll(castSpell.slotOptions.filter { it.pool == SpellSlotPool.Pact })
        }
    }

    var selectedOption by remember(castSpell.spellId) { mutableStateOf(defaultOption(slotOptions)) }

    LaunchedEffect(slotOptions) {
        val current = selectedOption
        if (current != null && slotOptions.any { it.pool == current.pool && it.slotLevel == current.slotLevel && it.enabled }) {
            return@LaunchedEffect
        }
        selectedOption = defaultOption(slotOptions)
    }

    val requiresSlotSelection = !isCantrip && !(showRitualToggle && castAsRitual)
    val canCast = !requiresSlotSelection || selectedOption?.enabled == true
    val castLabel = when {
        !requiresSlotSelection -> stringResource(R.string.spells_cast_action)
        selectedOption != null -> stringResource(R.string.spells_cast_action_with_level, selectedOption!!.slotLevel)
        else -> stringResource(R.string.spells_cast_action)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = castSpell.name,
            style = MaterialTheme.typography.titleLarge,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isCantrip) {
                Tag(label = stringResource(R.string.spells_cantrip_chip))
            } else {
                Tag(label = stringResource(R.string.spells_level_label, castSpell.level))
            }
            if (castSpell.isConcentration) {
                Tag(label = stringResource(R.string.spells_concentration_chip))
            }
            if (castSpell.isRitual) {
                Tag(label = stringResource(R.string.spells_ritual_chip))
            }
        }

        if (showRitualToggle) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.spells_cast_as_ritual),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = castAsRitual,
                    onCheckedChange = { castAsRitual = it },
                )
            }
        }

        if (requiresSlotSelection) {
            Text(
                text = stringResource(R.string.spells_select_slot_level),
                style = MaterialTheme.typography.titleSmall,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slotOptions.forEach { option ->
                    SlotOptionRow(
                        option = option,
                        selected = selectedOption?.pool == option.pool && selectedOption?.slotLevel == option.slotLevel,
                        onSelected = { selectedOption = option },
                    )
                }
            }

            val selected = selectedOption
            if (selected != null && selected.slotLevel > castSpell.level && castSpell.higherLevel.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.spells_at_higher_levels),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = castSpell.higherLevel.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.spells_no_slot_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.spells_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (!canCast) return@Button
                    if (!requiresSlotSelection) {
                        onCast(null, null, castAsRitual)
                    } else {
                        val option = selectedOption ?: return@Button
                        onCast(option.pool, option.slotLevel, castAsRitual)
                    }
                },
                enabled = canCast,
            ) {
                Text(text = castLabel)
            }
        }
    }
}

private fun defaultOption(options: List<CastSlotOptionUiModel>): CastSlotOptionUiModel? {
    return options
        .filter { it.enabled }
        .minWithOrNull(
            compareBy<CastSlotOptionUiModel> { it.slotLevel }
                .thenBy { if (it.pool == SpellSlotPool.Pact) 0 else 1 }
        )
}

@Composable
private fun Tag(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SlotOptionRow(
    option: CastSlotOptionUiModel,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (option.pool) {
        SpellSlotPool.Shared -> stringResource(R.string.spells_slot_option_shared, option.slotLevel)
        SpellSlotPool.Pact -> stringResource(R.string.spells_slot_option_pact, option.slotLevel)
    }
    val enabled = option.enabled
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (selected) 1.dp else 0.dp,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = if (enabled) onSelected else null,
                enabled = enabled,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.spells_slot_count, option.available, option.total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
