package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.StateFlow

interface AssetDataStore<T> : InitializableAssetDataStore {
    val data: StateFlow<AssetState<T>>
}
