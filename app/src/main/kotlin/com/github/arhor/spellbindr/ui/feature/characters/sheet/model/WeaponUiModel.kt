package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.DamageType

@Immutable
data class WeaponUiModel(
    val id: String,
    val name: String,
    val attackBonusLabel: String,
    val damageLabel: String,
    val damageType: DamageType,
)
