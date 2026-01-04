package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesRepository @Inject constructor(
    private val featuresDataStore: FeaturesAssetDataStore,
) {
    val allFeaturesState: Flow<Loadable<List<Feature>>>
        get() = featuresDataStore.data
}
