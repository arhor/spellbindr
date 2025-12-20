package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.TraitsAssetDataStore
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.TraitsRepository as TraitsRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraitsRepository @Inject constructor(
    private val traitsDataStore: TraitsAssetDataStore,
) : TraitsRepositoryContract {
    override val allTraits: Flow<List<Trait>>
        get() = traitsDataStore.data.map { traits -> traits.orEmpty().map { it.toDomain() } }
}
