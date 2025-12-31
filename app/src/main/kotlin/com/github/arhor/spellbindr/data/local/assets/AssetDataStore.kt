package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.StateFlow

interface AssetDataStore<T> : InitializableAssetDataStore {
    val data: StateFlow<Loadable<T>>
}
