package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.SpellsRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SpellsDomainModule {

    @Binds
    abstract fun bindSpellsRepository(impl: SpellsRepositoryImpl): SpellsRepository
}
