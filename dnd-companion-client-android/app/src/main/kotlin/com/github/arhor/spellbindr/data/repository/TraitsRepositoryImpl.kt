package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.TraitsAssetDataStore
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraitsRepositoryImpl @Inject constructor(
    private val traitsDataStore: TraitsAssetDataStore,
) : TraitsRepository {

    override val allTraitsState: Flow<Loadable<List<Trait>>>
        get() = traitsDataStore.data
}
