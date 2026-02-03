package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellcastingClassUiModel(
    val sourceKey: String,
    val name: String,
    val spellcastingAbilityLabel: String,
    val spellSaveDcLabel: String,
    val spellAttackBonusLabel: String,
    val spellLevels: List<SpellLevelUiModel>,
)

