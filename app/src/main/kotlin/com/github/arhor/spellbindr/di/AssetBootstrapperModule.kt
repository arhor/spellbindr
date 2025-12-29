package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.assets.DefaultAssetBootstrapper
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AssetBootstrapperModule {

    @Binds
    abstract fun bindAssetBootstrapper(bootstrapper: DefaultAssetBootstrapper): AssetBootstrapper
}
