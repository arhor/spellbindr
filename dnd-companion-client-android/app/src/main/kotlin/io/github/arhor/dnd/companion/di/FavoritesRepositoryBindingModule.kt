package io.github.arhor.dnd.companion.di

import io.github.arhor.dnd.companion.data.repository.FavoritesRepositoryImpl
import io.github.arhor.dnd.companion.domain.repository.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesRepositoryBindingModule {
    @Binds
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository
}
