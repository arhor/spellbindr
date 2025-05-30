package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.datasource.local.CharacterClassesAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.InitializingStaticAssetDataStore
import com.github.arhor.spellbindr.data.datasource.local.SpellsAssetDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@Suppress("UNUSED")
@InstallIn(SingletonComponent::class)
abstract class StaticAssetLoaderModule {

    @Binds
    @IntoSet
    abstract fun bindSpellsAssetDataStore(spellsDataStore: SpellsAssetDataStore)
        : InitializingStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindCharacterClassesAssetDataStore(characterClassesDataStore: CharacterClassesAssetDataStore)
        : InitializingStaticAssetDataStore
}
