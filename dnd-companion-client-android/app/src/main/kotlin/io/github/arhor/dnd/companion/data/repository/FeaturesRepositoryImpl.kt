package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.FeaturesAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Feature
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.FeaturesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturesRepositoryImpl @Inject constructor(
    private val featuresDataStore: FeaturesAssetDataStore,
) : FeaturesRepository {
    override val allFeaturesState: Flow<Loadable<List<Feature>>>
        get() = featuresDataStore.data
}

