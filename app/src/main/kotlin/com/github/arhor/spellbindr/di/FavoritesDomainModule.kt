package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.FavoritesRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesDomainModule {

    @Binds
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository
}
