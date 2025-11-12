package com.github.arhor.spellbindr.di

import android.content.Context
import androidx.room.Room
import com.github.arhor.spellbindr.data.local.db.CharacterDao
import com.github.arhor.spellbindr.data.local.db.SpellbindrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSpellbindrDatabase(@ApplicationContext context: Context): SpellbindrDatabase =
        Room.databaseBuilder<SpellbindrDatabase>(context, "spellbindr.db")
            .addMigrations(SpellbindrDatabase.MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideCharacterDao(database: SpellbindrDatabase): CharacterDao =
        database.characterDao()
} 
