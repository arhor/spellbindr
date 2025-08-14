package com.github.arhor.spellbindr.data.local.assets

import kotlinx.coroutines.flow.StateFlow

interface StaticAssetDataStore<T> : InitializableStaticAssetDataStore {
    val data: StateFlow<T?>
}
