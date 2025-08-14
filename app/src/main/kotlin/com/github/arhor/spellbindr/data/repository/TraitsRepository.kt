package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.TraitsAssetDataStore
import com.github.arhor.spellbindr.data.model.Trait
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraitsRepository @Inject constructor(
    private val traitsDataStore: TraitsAssetDataStore,
) {
    val allTraits: Flow<List<Trait>>
        get() = traitsDataStore.data.map { it ?: emptyList() }
}