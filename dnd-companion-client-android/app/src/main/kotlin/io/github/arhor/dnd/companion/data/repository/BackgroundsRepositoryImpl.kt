package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.BackgroundsAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Background
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.BackgroundsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundsRepositoryImpl @Inject constructor(
    private val backgroundsDataStore: BackgroundsAssetDataStore,
) : BackgroundsRepository {
    override val allBackgroundsState: Flow<Loadable<List<Background>>>
        get() = backgroundsDataStore.data

    override suspend fun findBackgroundById(id: String): Background? =
        when (val state = backgroundsDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data.find { it.id == id }
            is Loadable.Failure -> null
            is Loadable.Loading -> null
        }
}
