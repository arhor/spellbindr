package com.github.arhor.spellbindr.data.datasource.local.assets

import kotlinx.coroutines.flow.StateFlow

interface StaticAssetDataStore<T> : InitializingStaticAssetDataStore {
    val data: StateFlow<T?>
}
