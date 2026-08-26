package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.FeatsAssetDataStore
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.FeatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatsRepositoryImpl @Inject constructor(
    private val featsDataStore: FeatsAssetDataStore,
) : FeatsRepository {

    override val allFeatsState: Flow<Loadable<List<Feat>>>
        get() = featsDataStore.data
}
