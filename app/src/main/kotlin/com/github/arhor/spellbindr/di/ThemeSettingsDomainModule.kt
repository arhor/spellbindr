package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.ThemeRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeSettingsDomainModule {
    @Binds
    abstract fun bindThemeSettingsRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
