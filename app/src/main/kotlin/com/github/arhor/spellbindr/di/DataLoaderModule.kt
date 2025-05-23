package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.DataLoader
import com.github.arhor.spellbindr.data.repository.RaceRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@Suppress("UNUSED")
@InstallIn(SingletonComponent::class)
abstract class DataLoaderModule {

    @Binds
    @IntoSet
    abstract fun bindSpellRepository(spellRepository: SpellRepository): DataLoader

    @Binds
    @IntoSet
    abstract fun bindRaceRepository(raceRepository: RaceRepository): DataLoader
}
