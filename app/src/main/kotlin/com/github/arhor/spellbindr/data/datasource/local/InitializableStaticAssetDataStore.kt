package com.github.arhor.spellbindr.data.datasource.local

interface InitializingStaticAssetDataStore {
    suspend fun initialize()
}
