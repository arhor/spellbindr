package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.dataOrNull
import com.github.arhor.spellbindr.domain.model.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesRepository @Inject constructor(
    private val featuresDataStore: FeaturesAssetDataStore,
) {
    val allFeatures: Flow<List<Feature>>
        get() = featuresDataStore.data.map { it.dataOrNull().orEmpty() }
}

