package com.github.arhor.spellbindr.core.assets

import kotlinx.coroutines.flow.StateFlow

interface StaticAssetDataStore<T> : InitializableStaticAssetDataStore {
    val data: StateFlow<T?>
}
