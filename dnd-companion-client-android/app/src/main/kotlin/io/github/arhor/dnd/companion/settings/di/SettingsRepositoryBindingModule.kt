package io.github.arhor.dnd.companion.settings.di

import io.github.arhor.dnd.companion.domain.repository.SettingsRepository
import io.github.arhor.dnd.companion.settings.data.repository.SettingsRepositoryImpl
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
