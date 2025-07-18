package com.github.arhor.spellbindr.data.spells

import com.github.arhor.spellbindr.core.assets.InitializableStaticAssetDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@Suppress("UNUSED")
@InstallIn(SingletonComponent::class)
abstract class SpellAssetModule {

    @Binds
    @IntoSet
    abstract fun bindSpellsAssetDataStore(spellsDataStore: SpellAssetDataStore)
        : InitializableStaticAssetDataStore
}
