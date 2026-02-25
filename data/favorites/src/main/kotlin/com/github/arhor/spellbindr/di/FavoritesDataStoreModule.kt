package com.github.arhor.spellbindr.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavoritesDataStoreModule {

    @Provides
    @Singleton
    @FavoritesDataStore
    fun provideFavoritesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = EMPTY_PREFERENCES_ON_CORRUPTION_HANDLER,
        scope = PREFERENCE_DATA_STORE_SCOPE,
        produceFile = { context.preferencesDataStoreFile(FAVORITES_DATASTORE_NAME) },
    )

    private const val FAVORITES_DATASTORE_NAME = "favorites.preferences_pb"
    private val EMPTY_PREFERENCES_ON_CORRUPTION_HANDLER = ReplaceFileCorruptionHandler { emptyPreferences() }
    private val PREFERENCE_DATA_STORE_SCOPE = CoroutineScope(Dispatchers.IO + SupervisorJob())
}
