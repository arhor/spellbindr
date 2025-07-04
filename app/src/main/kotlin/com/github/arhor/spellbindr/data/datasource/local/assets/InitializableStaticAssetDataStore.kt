package com.github.arhor.spellbindr.data.datasource.local.assets

interface InitializingStaticAssetDataStore {
    suspend fun initialize()
}
