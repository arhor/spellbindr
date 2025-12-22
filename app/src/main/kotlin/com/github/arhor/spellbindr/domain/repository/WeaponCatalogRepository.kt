package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import kotlinx.coroutines.flow.Flow

interface WeaponCatalogRepository {
    val weaponCatalogEntries: Flow<List<WeaponCatalogEntry>>
}
