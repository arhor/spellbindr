package com.github.arhor.spellbindr.di

import android.content.Context
import androidx.room.Room
import com.github.arhor.spellbindr.data.local.database.Converters
import com.github.arhor.spellbindr.data.local.database.SpellbindrDatabase
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.dao.FavoritesDao
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
    fun provideSpellbindrDatabase(
        @ApplicationContext
        context: Context,
        converters: Converters,
    ): SpellbindrDatabase =
        Room.databaseBuilder<SpellbindrDatabase>(context, "spellbindr.db")
            .addTypeConverter(converters)
            .addMigrations(*SpellbindrDatabase.allMigrations)
            .build()

    @Provides
    @Singleton
    fun provideCharacterDao(db: SpellbindrDatabase): CharacterDao =
        db.characterDao()

    @Provides
    fun provideFavoritesDao(db: SpellbindrDatabase): FavoritesDao =
        db.favoritesDao()
}
