package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.CharacterRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CharactersDomainModule {
    @Binds
    abstract fun bindCharactersRepository(impl: CharacterRepositoryImpl): CharacterRepository
}
