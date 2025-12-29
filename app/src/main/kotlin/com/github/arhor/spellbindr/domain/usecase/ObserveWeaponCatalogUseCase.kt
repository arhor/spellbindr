package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.data.mapper.toWeaponCatalogEntryOrNull
import com.github.arhor.spellbindr.data.repository.EquipmentRepository
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveWeaponCatalogUseCase @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
) {
    operator fun invoke(): Flow<List<WeaponCatalogEntry>> = equipmentRepository.allEquipmentState.map {
        when (it) {
            is AssetState.Loading -> emptyList()
            is AssetState.Ready -> it.data.mapNotNull { it.toWeaponCatalogEntryOrNull() }
            is AssetState.Error -> emptyList()
        }
    }
}
