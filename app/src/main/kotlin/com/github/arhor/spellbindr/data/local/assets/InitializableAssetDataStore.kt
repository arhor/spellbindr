package com.github.arhor.spellbindr.data.local.assets

interface InitializableAssetDataStore {
    val priority: AssetLoadingPriority

    suspend fun initialize()
}
