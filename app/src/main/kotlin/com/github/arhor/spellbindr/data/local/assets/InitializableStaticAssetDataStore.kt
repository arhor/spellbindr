package com.github.arhor.spellbindr.data.local.assets

interface InitializableStaticAssetDataStore {
    suspend fun initialize()
}
