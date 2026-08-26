package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.DamageType

@Immutable
data class WeaponUiModel(
    val id: String,
    val name: String,
    val attackBonusLabel: String,
    val damageLabel: String,
    val damageType: DamageType,
)
