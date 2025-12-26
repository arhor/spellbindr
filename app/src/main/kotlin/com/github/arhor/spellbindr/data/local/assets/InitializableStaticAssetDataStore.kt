package com.github.arhor.spellbindr.data.local.assets

interface InitializableStaticAssetDataStore {
    val loadingPriority: AssetLoadingPriority

    suspend fun initialize()
}
