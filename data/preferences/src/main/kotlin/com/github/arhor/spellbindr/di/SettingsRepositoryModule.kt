package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.FavoritesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.ThemeRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Binds
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
