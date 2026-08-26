package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.FeatsAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Feat
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.FeatsRepository
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
