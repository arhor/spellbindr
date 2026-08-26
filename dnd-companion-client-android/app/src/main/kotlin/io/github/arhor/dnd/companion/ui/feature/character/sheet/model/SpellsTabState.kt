package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellsTabState(
    val spellcastingClasses: List<SpellcastingClassUiModel>,
    val canAddSpells: Boolean,
    val sharedSlots: List<SpellSlotUiModel>,
    val hasConfiguredSharedSlots: Boolean,
    val pactSlots: PactSlotUiModel?,
    val concentration: ConcentrationUiModel?,
)
