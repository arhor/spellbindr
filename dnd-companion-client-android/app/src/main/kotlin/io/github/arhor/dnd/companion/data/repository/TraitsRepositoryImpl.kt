package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.TraitsAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Trait
import io.github.arhor.dnd.companion.domain.repository.TraitsRepository
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
