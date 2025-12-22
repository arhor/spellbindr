package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import com.github.arhor.spellbindr.domain.repository.WeaponCatalogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWeaponCatalogUseCase @Inject constructor(
    private val weaponCatalogRepository: WeaponCatalogRepository,
) {
    operator fun invoke(): Flow<List<WeaponCatalogEntry>> = weaponCatalogRepository.weaponCatalogEntries
}
