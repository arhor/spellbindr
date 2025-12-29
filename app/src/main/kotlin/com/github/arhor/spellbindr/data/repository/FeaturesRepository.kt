package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Feature
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesRepository @Inject constructor(
    private val featuresDataStore: FeaturesAssetDataStore,
) {
    val allFeaturesState: Flow<AssetState<List<Feature>>>
        get() = featuresDataStore.data
}
