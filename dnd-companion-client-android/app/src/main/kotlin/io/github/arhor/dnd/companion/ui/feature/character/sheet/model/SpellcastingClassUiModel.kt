package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellcastingClassUiModel(
    val sourceKey: String,
    val name: String,
    val isUnassigned: Boolean,
    val spellcastingAbility: String?,
    val spellSaveDc: Int?,
    val spellAttackBonus: Int?,
    val spellLevels: List<SpellLevelUiModel>,
)
