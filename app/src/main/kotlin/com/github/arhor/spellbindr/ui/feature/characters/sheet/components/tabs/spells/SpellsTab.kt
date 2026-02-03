package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellsTabState
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellsTab(
    spellsState: SpellsTabState,
    editMode: SheetEditMode,
    onAddSpellsClick: () -> Unit,
    onSpellSlotToggle: (Int, Int) -> Unit,
    onSpellSlotTotalChanged: (Int, Int) -> Unit,
    onPactSlotToggle: (Int) -> Unit,
    onPactSlotTotalChanged: (Int) -> Unit,
    onConcentrationClear: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val hasSpells = spellsState.spellcastingClasses.isNotEmpty()
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(16.dp),
    ) {
        item(key = "spells-dashboard") {
            CastingDashboardCard(
                sharedSlots = spellsState.sharedSlots,
                hasConfiguredSharedSlots = spellsState.hasConfiguredSharedSlots,
                pactSlots = spellsState.pactSlots,
                concentration = spellsState.concentration,
                editMode = editMode,
                onSharedSlotToggle = onSpellSlotToggle,
                onSharedSlotTotalChanged = onSpellSlotTotalChanged,
                onPactSlotToggle = onPactSlotToggle,
                onPactSlotTotalChanged = onPactSlotTotalChanged,
                onConcentrationClear = onConcentrationClear,
            )
        }
        item(key = "spells-dashboard-spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (spellsState.canAddSpells) {
            item(key = "spells-add") {
                FilledTonalButton(
                    onClick = onAddSpellsClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.spells_add_spells))
                }
            }
            item(key = "spells-add-spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (!hasSpells) {
            item(key = "spells-empty") {
                Text(
                    text = stringResource(R.string.spells_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item(key = "spells-empty-spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        spellsState.spellcastingClasses.forEachIndexed { index, spellcastingClass ->
            spellcastingClassSection(
                spellcastingClass = spellcastingClass,
                editMode = editMode,
                onSpellSelected = onSpellSelected,
                onSpellRemoved = onSpellRemoved,
            )
            if (index < spellsState.spellcastingClasses.lastIndex) {
                item(key = "spells-section-spacer-${spellcastingClass.sourceKey}") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
internal fun SpellsTabPreview() {
    AppTheme {
        SpellsTab(
            spellsState = CharacterSheetPreviewData.spells,
            editMode = SheetEditMode.View,
            onAddSpellsClick = {},
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onPactSlotToggle = {},
            onPactSlotTotalChanged = {},
            onConcentrationClear = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            listState = androidx.compose.foundation.lazy.rememberLazyListState(),
        )
    }
}

private fun LazyListScope.spellcastingClassSection(
    spellcastingClass: SpellcastingClassUiModel,
    editMode: SheetEditMode,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
) {
    val rows = buildList<SpellClassRow> {
        add(SpellClassRow.Header(spellcastingClass))
        if (spellcastingClass.spellLevels.isEmpty()) {
            add(SpellClassRow.Empty(spellcastingClass.sourceKey))
        } else {
            spellcastingClass.spellLevels.forEach { level ->
                add(SpellClassRow.LevelHeader(spellcastingClass.sourceKey, level))
                level.spells.forEach { spell ->
                    add(SpellClassRow.Spell(spellcastingClass.sourceKey, spell))
                }
            }
        }
    }
    itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
        val shape = classSectionShape(index, rows.lastIndex, MaterialTheme.shapes.large)
        Surface(
            shape = shape,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (row) {
                is SpellClassRow.Header -> {
                    SpellcastingClassHeader(
                        spellcastingClass = row.spellcastingClass,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                is SpellClassRow.Empty -> {
                    Text(
                        text = stringResource(R.string.spells_no_spells_for_class),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                is SpellClassRow.LevelHeader -> {
                    SpellLevelHeader(
                        spellLevel = row.spellLevel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                is SpellClassRow.Spell -> {
                    SpellRow(
                        spell = row.spell,
                        editMode = editMode,
                        onClick = { onSpellSelected(row.spell.spellId) },
                        onRemove = { onSpellRemoved(row.spell.spellId, row.spell.sourceClass) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private sealed interface SpellClassRow {
    val key: String

    data class Header(val spellcastingClass: SpellcastingClassUiModel) : SpellClassRow {
        override val key: String = "class-header-${spellcastingClass.sourceKey}"
    }

    data class Empty(val sourceKey: String) : SpellClassRow {
        override val key: String = "class-empty-$sourceKey"
    }

    data class LevelHeader(val sourceKey: String, val spellLevel: SpellLevelUiModel) : SpellClassRow {
        override val key: String = "class-level-$sourceKey-${spellLevel.level}"
    }

    data class Spell(val sourceKey: String, val spell: CharacterSpellUiModel) : SpellClassRow {
        override val key: String = "class-spell-$sourceKey-${spell.spellId}-${spell.sourceClass}"
    }
}

private fun classSectionShape(
    index: Int,
    lastIndex: Int,
    baseShape: androidx.compose.ui.graphics.Shape,
): androidx.compose.ui.graphics.Shape {
    val cornerShape = baseShape as? CornerBasedShape ?: return baseShape
    return when {
        index == 0 && index == lastIndex -> baseShape
        index == 0 -> cornerShape.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        )

        index == lastIndex -> cornerShape.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
        )

        else -> cornerShape.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        )
    }
}
