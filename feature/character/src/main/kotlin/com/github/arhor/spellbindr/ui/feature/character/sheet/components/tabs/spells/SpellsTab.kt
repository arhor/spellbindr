package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.feature.character.R
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellsTabState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsTab(
    spellsState: SpellsTabState,
    editMode: SheetEditMode,
    onAddSpellsClick: () -> Unit,
    onCastSpellClick: (String) -> Unit,
    onLongRestClick: () -> Unit,
    onShortRestClick: () -> Unit,
    onConfigureSlotsClick: () -> Unit,
    onSpellSlotToggle: (Int, Int) -> Unit,
    onSpellSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onPactSlotLevelChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var castingTimeFilter by rememberSaveable { mutableStateOf<CastingTimeFilter?>(null) }
    var concentrationOnly by rememberSaveable { mutableStateOf(false) }
    var ritualOnly by rememberSaveable { mutableStateOf(false) }
    var sort by rememberSaveable { mutableStateOf(SpellSort.Name) }

    val visibleClasses by remember(
        spellsState.spellcastingClasses,
        castingTimeFilter,
        concentrationOnly,
        ritualOnly,
        sort,
    ) {
        derivedStateOf {
            spellsState.spellcastingClasses.mapNotNull { spellcastingClass ->
                val filteredLevels = filterAndSortSpellLevels(
                    spellLevels = spellcastingClass.spellLevels,
                    castingTime = castingTimeFilter,
                    concentrationOnly = concentrationOnly,
                    ritualOnly = ritualOnly,
                    sort = sort,
                )
                if (filteredLevels.isEmpty()) return@mapNotNull null
                spellcastingClass.copy(spellLevels = filteredLevels)
            }
        }
    }

    val canCastFor: (CharacterSpellUiModel) -> Boolean = remember(spellsState.sharedSlots, spellsState.pactSlots) {
        { spell ->
            canCastSpell(
                spellLevel = spell.level,
                sharedSlots = spellsState.sharedSlots,
                pactSlots = spellsState.pactSlots,
            )
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "spells-dashboard") {
            CastingDashboardCard(
                sharedSlots = spellsState.sharedSlots,
                hasConfiguredSharedSlots = spellsState.hasConfiguredSharedSlots,
                pactSlots = spellsState.pactSlots,
                concentration = spellsState.concentration,
                editMode = editMode,
                onLongRestClick = onLongRestClick,
                onShortRestClick = onShortRestClick,
                onConfigureSlotsClick = onConfigureSlotsClick,
                onSharedSlotToggle = onSpellSlotToggle,
                onSharedSlotTotalChanged = onSpellSlotTotalChanged,
                onPactSlotToggle = onPactSlotToggle,
                onPactSlotTotalChanged = onPactSlotTotalChanged,
                onPactSlotLevelChanged = onPactSlotLevelChanged,
                onConcentrationClear = onConcentrationClear,
            )
        }

        item(key = "spells-filters") {
            SpellFiltersRow(
                castingTimeFilter = castingTimeFilter,
                concentrationOnly = concentrationOnly,
                ritualOnly = ritualOnly,
                sort = sort,
                showAddSpells = editMode == SheetEditMode.Edit && spellsState.canAddSpells,
                onCastingTimeSelected = { selected ->
                    castingTimeFilter = if (castingTimeFilter == selected) null else selected
                },
                onConcentrationToggle = { concentrationOnly = !concentrationOnly },
                onRitualToggle = { ritualOnly = !ritualOnly },
                onSortSelected = { sort = it },
                onAddSpellsClick = onAddSpellsClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        when {
            spellsState.spellcastingClasses.isEmpty() -> {
                item(key = "spells-empty") {
                    Text(
                        text = stringResource(R.string.spells_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            visibleClasses.isEmpty() -> {
                item(key = "spells-filters-empty") {
                    Text(
                        text = stringResource(R.string.spells_no_spells_match_filters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                itemsIndexed(
                    visibleClasses,
                    key = { _, spellcastingClass -> spellcastingClass.sourceKey },
                ) { index, spellcastingClass ->
                    SpellcastingClassCard(
                        spellcastingClass = spellcastingClass,
                        sharedSlots = spellsState.sharedSlots,
                        pactSlots = spellsState.pactSlots,
                        showSharedSlotBadges = index == 0,
                        editMode = editMode,
                        canCastFor = canCastFor,
                        onCastSpellClick = onCastSpellClick,
                        onSpellSelected = onSpellSelected,
                        onSpellRemoved = onSpellRemoved,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpellFiltersRow(
    castingTimeFilter: CastingTimeFilter?,
    concentrationOnly: Boolean,
    ritualOnly: Boolean,
    sort: SpellSort,
    showAddSpells: Boolean,
    onCastingTimeSelected: (CastingTimeFilter) -> Unit,
    onConcentrationToggle: () -> Unit,
    onRitualToggle: () -> Unit,
    onSortSelected: (SpellSort) -> Unit,
    onAddSpellsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.outline,
        selectedLabelColor = MaterialTheme.colorScheme.surfaceBright,
        selectedLeadingIconColor = MaterialTheme.colorScheme.surfaceBright,
    )
    val chipBorder = FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = false,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        selectedBorderColor = Color.Transparent,
        borderWidth = 1.dp,
        selectedBorderWidth = 0.dp,
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "filter-action") {
            FilterChip(
                selected = castingTimeFilter == CastingTimeFilter.Action,
                onClick = { onCastingTimeSelected(CastingTimeFilter.Action) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = null,
                    )
                },
                label = { Text("Action", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "filter-bonus") {
            FilterChip(
                selected = castingTimeFilter == CastingTimeFilter.Bonus,
                onClick = { onCastingTimeSelected(CastingTimeFilter.Bonus) },
                label = { Text("Bonus", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "filter-reaction") {
            FilterChip(
                selected = castingTimeFilter == CastingTimeFilter.Reaction,
                onClick = { onCastingTimeSelected(CastingTimeFilter.Reaction) },
                label = { Text("Reaction", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "filter-conc") {
            FilterChip(
                selected = concentrationOnly,
                onClick = onConcentrationToggle,
                label = { Text("Conc", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "filter-ritual") {
            FilterChip(
                selected = ritualOnly,
                onClick = onRitualToggle,
                label = { Text("Ritual", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "sort-name") {
            FilterChip(
                selected = sort == SpellSort.Name,
                onClick = { onSortSelected(SpellSort.Name) },
                label = { Text("Name", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }
        item(key = "sort-level") {
            FilterChip(
                selected = sort == SpellSort.Level,
                onClick = { onSortSelected(SpellSort.Level) },
                label = { Text("Level", style = MaterialTheme.typography.labelMedium) },
                colors = chipColors,
                border = chipBorder,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier,
            )
        }

        if (showAddSpells) {
            item(key = "add-spells") {
                FilledTonalIconButton(onClick = onAddSpellsClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.spells_add_spells),
                    )
                }
            }
        }
    }
}

private fun canCastSpell(
    spellLevel: Int,
    sharedSlots: List<SpellSlotUiModel>,
    pactSlots: PactSlotUiModel?,
): Boolean {
    if (spellLevel <= 0) return true
    val hasShared = sharedSlots.any { slot ->
        slot.level >= spellLevel && (slot.total - slot.expended) > 0
    }
    if (hasShared) return true
    val pactLevel = pactSlots?.slotLevel ?: return false
    val pactAvailable = (pactSlots.total - pactSlots.expended).coerceAtLeast(0)
    return pactAvailable > 0 && pactLevel >= spellLevel
}

@Composable
@PreviewLightDark
internal fun SpellsTabPreview() {
    AppTheme {
        SpellsTab(
            spellsState = CharacterSheetPreviewData.spells,
            editMode = SheetEditMode.View,
            onAddSpellsClick = {},
            onCastSpellClick = {},
            onLongRestClick = {},
            onShortRestClick = {},
            onConfigureSlotsClick = {},
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onPactSlotToggle = {},
            onPactSlotTotalChanged = {},
            onPactSlotLevelChanged = {},
            onConcentrationClear = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            listState = androidx.compose.foundation.lazy.rememberLazyListState(),
        )
    }
}
