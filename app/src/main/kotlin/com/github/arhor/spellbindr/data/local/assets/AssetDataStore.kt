package com.github.arhor.spellbindr.data.local.assets

import kotlinx.coroutines.flow.StateFlow

interface AssetDataStore<T> : InitializableAssetDataStore {
    val data: StateFlow<T?>
}
