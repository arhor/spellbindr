package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.RaceRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import com.github.arhor.spellbindr.data.repository.StaticAssetLoader
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
    abstract fun bindSpellRepository(repository: SpellRepository): StaticAssetLoader

    @Binds
    @IntoSet
    abstract fun bindRaceRepository(repository: RaceRepository): StaticAssetLoader
}
