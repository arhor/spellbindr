package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityId

@Immutable
data class AbilityUiModel(
    val abilityId: AbilityId,
    val label: String,
    val score: Int,
    val modifier: Int,
    val savingThrowBonus: Int,
    val savingThrowProficient: Boolean,
)
