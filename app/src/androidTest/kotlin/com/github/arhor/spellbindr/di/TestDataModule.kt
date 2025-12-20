package com.github.arhor.spellbindr.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.github.arhor.spellbindr.data.local.db.CharacterDao
import com.github.arhor.spellbindr.data.local.db.FavoritesDao
import com.github.arhor.spellbindr.data.local.db.SpellbindrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class, DatabaseModule::class],
)
object TestDataModule {

    @Provides
    @Singleton
    @AppSettingsDataStore
    fun provideTestSettingsDataStore(): DataStore<Preferences> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        ) {
            context.preferencesDataStoreFile(TEST_SETTINGS_DATASTORE_NAME)
        }
    }

    @Provides
    @Singleton
    @FavoritesDataStore
    fun provideTestFavoritesDataStore(): DataStore<Preferences> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        ) {
            context.preferencesDataStoreFile(TEST_FAVORITES_DATASTORE_NAME)
        }
    }

    @Provides
    @Singleton
    fun provideTestDatabase(): SpellbindrDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, SpellbindrDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideCharacterDao(database: SpellbindrDatabase): CharacterDao =
        database.characterDao()

    @Provides
    fun provideFavoritesDao(database: SpellbindrDatabase): FavoritesDao =
        database.favoritesDao()

    private const val TEST_SETTINGS_DATASTORE_NAME = "test_app_settings.preferences_pb"
    private const val TEST_FAVORITES_DATASTORE_NAME = "test_favorites.preferences_pb"
}
