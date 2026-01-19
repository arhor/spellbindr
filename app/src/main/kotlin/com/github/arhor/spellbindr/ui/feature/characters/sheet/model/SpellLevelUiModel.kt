package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellLevelUiModel(
    val level: Int,
    val label: String,
    val spellSlot: SpellSlotUiModel?,
    val spells: List<CharacterSpellUiModel>,
)
