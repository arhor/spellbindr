package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory

@Immutable
data class WeaponCatalogUiModel(
    val id: String,
    val name: String,
    val category: EquipmentCategory?,
    val categories: Set<EquipmentCategory>,
    val damageDiceCount: Int,
    val damageDieSize: Int,
    val damageType: DamageType,
)
