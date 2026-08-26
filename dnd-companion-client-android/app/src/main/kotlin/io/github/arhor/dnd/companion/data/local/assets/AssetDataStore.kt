package io.github.arhor.dnd.companion.data.local.assets

import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.StateFlow

interface AssetDataStore<T> {
    val data: StateFlow<Loadable<T>>
    val priority: AssetLoadingPriority

    suspend fun initialize()
}
