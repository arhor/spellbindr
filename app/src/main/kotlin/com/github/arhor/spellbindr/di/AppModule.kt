package com.github.arhor.spellbindr.di

import android.content.Context
import com.github.arhor.spellbindr.data.repository.SpellRepository
import com.github.arhor.spellbindr.data.repository.SettingsRepository
import com.github.arhor.spellbindr.data.repository.SpellListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSpellRepository(@ApplicationContext context: Context) =
        SpellRepository(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) =
        SettingsRepository(context)

    @Provides
    @Singleton
    fun provideSpellListRepository(@ApplicationContext context: Context) =
        SpellListRepository(context)
}
