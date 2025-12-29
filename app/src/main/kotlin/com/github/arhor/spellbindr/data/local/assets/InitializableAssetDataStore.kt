package com.github.arhor.spellbindr.data.local.assets

interface InitializableAssetDataStore {
    val loadingPriority: AssetLoadingPriority

    suspend fun initialize()
}
