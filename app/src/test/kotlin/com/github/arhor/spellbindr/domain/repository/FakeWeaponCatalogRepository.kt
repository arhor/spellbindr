package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWeaponCatalogRepository(
    initialEntries: List<WeaponCatalogEntry> = emptyList(),
) : WeaponCatalogRepository {

    private val entries = MutableStateFlow(initialEntries)

    override val weaponCatalogEntries: Flow<List<WeaponCatalogEntry>> = entries.asStateFlow()

    fun update(entries: List<WeaponCatalogEntry>) {
        this.entries.value = entries
    }
}
