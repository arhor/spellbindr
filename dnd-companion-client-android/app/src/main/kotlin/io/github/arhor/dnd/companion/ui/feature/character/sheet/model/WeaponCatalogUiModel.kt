package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.DamageType
import io.github.arhor.dnd.companion.domain.model.EquipmentCategory

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
