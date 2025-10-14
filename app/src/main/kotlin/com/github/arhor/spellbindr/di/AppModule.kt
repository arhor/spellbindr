package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.DiceRollRepository
import com.github.arhor.spellbindr.data.repository.DiceRollRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDiceRollRepository(
        diceRollRepositoryImpl: DiceRollRepositoryImpl
    ): DiceRollRepository
}
