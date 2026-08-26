package io.github.arhor.dnd.companion.di

import io.github.arhor.dnd.companion.data.repository.CharacterRepositoryImpl
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CharacterRepositoryModule {
    @Binds
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository
}
