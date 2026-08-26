package io.github.arhor.dnd.companion.di

import android.content.Context
import androidx.room.Room
import io.github.arhor.dnd.companion.data.local.database.SpellbindrDatabase
import io.github.arhor.dnd.companion.data.local.database.converter.Converter
import io.github.arhor.dnd.companion.data.local.database.dao.CharacterDao
import io.github.arhor.dnd.companion.data.local.database.dao.FavoritesDao
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
        converters: Set<@JvmSuppressWildcards Converter>,
    ): SpellbindrDatabase =
        Room.databaseBuilder<SpellbindrDatabase>(context, "spellbindr.db")
            .apply { converters.forEach(::addTypeConverter) }
            .build()

    @Provides
    @Singleton
    fun provideCharacterDao(db: SpellbindrDatabase): CharacterDao =
        db.characterDao()

    @Provides
    fun provideFavoritesDao(db: SpellbindrDatabase): FavoritesDao =
        db.favoritesDao()
}
