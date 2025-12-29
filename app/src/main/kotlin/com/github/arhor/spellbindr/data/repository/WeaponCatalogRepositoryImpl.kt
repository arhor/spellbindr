package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.mapper.toWeaponCatalogEntryOrNull
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import com.github.arhor.spellbindr.domain.repository.WeaponCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class WeaponCatalogRepositoryImpl @Inject constructor(
    private val equipmentRepository: EquipmentRepository,
) : WeaponCatalogRepository {
    override val weaponCatalogEntries: Flow<List<WeaponCatalogEntry>>
        get() = equipmentRepository.allEquipmentState.map { state ->
            when (state) {
                is AssetState.Loading -> emptyList()
                is AssetState.Ready -> state.data.mapNotNull { it.toWeaponCatalogEntryOrNull() }
                is AssetState.Error -> emptyList()
            }
        }
}
