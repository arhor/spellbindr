package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.datasource.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.assets.CharacterClassesAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.assets.InitializingStaticAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.assets.RacesAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.assets.SpellsAssetDataStore
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
    abstract fun bindSpellsAssetDataStore(spellsDataStore: SpellsAssetDataStore)
        : InitializingStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindCharacterClassesAssetDataStore(characterClassesDataStore: CharacterClassesAssetDataStore)
        : InitializingStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindBackgroundsAssetDataStore(backgroundsDataStore: BackgroundsAssetDataStore)
        : InitializingStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindEquipmentAssetDataStore(equipmentDataStore: EquipmentAssetDataStore)
        : InitializingStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindRacesAssetDataStore(dacesAssetDataStore: RacesAssetDataStore)
        : InitializingStaticAssetDataStore
}
