package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.CharacterClassRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CharacterClassesDomainModule {

    @Binds
    abstract fun bindCharacterClassRepository(
        impl: CharacterClassRepositoryImpl,
    ): CharacterClassRepository
}
