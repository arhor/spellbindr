package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.StateFlow

interface AssetDataStore<T> {
    val data: StateFlow<Loadable<T>>
    val priority: AssetLoadingPriority

    suspend fun initialize()
}
