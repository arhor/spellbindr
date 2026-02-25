package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.SettingsRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryBindingModule {
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
