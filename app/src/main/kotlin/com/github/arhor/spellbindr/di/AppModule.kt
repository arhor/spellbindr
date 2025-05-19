package com.github.arhor.spellbindr.di

import android.content.Context
import com.github.arhor.spellbindr.data.repository.RaceRepository
import com.github.arhor.spellbindr.data.repository.SpellRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    @Provides
    @Singleton
    fun provideSpellRepository(
        @ApplicationContext
        context: Context,
        json: Json,
    ) = SpellRepository(context, json)

    @Provides
    @Singleton
    fun provideRaceRepository(
        @ApplicationContext
        context: Context,
        json: Json,
    ) = RaceRepository(context, json)
}
