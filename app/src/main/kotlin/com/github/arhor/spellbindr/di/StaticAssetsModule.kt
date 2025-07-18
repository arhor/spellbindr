package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.core.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.RacesAssetDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@Suppress("UNUSED")
@InstallIn(SingletonComponent::class)
abstract class StaticAssetsModule {

    @Binds
    @IntoSet
    abstract fun bindBackgroundsAssetDataStore(backgroundsDataStore: BackgroundsAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindEquipmentAssetDataStore(equipmentDataStore: EquipmentAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindRacesAssetDataStore(dacesAssetDataStore: RacesAssetDataStore)
        : InitializableStaticAssetDataStore
}
