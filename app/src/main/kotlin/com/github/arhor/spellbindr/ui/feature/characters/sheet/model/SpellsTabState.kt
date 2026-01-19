package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellsTabState(
    val spellLevels: List<SpellLevelUiModel>,
    val canAddSpells: Boolean,
    val sharedSlots: List<SpellSlotUiModel>,
    val pactSlots: PactSlotUiModel?,
    val concentration: ConcentrationUiModel?,
    val sourceFilters: List<SpellSourceFilterUiModel>,
    val selectedSourceId: String?,
    val showSourceBadges: Boolean,
    val showSourceFilters: Boolean,
)
