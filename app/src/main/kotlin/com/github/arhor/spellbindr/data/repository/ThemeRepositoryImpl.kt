package com.github.arhor.spellbindr.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.arhor.spellbindr.data.mapper.toData
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.di.AppSettingsDataStore
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.utils.Logger.Companion.createLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @AppSettingsDataStore private val dataStore: DataStore<Preferences>,
) : ThemeRepository {

    override val themeMode: Flow<ThemeMode?>
        get() = dataStore.data.map { preferences ->
            preferences[THEME_MODE]?.let { storedValue ->
                runCatching { AppThemeMode.valueOf(storedValue) }
                    .getOrElse { error ->
                        logger.error(error) { "Invalid theme mode stored: $storedValue" }
                        null
                    }
            }?.toDomain()
        }

    override suspend fun setThemeMode(mode: ThemeMode?) {
        dataStore.edit { preferences ->
            if (mode == null) {
                preferences.remove(THEME_MODE)
            } else {
                preferences[THEME_MODE] = mode.toData().name
            }
        }
    }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val logger = createLogger<ThemeRepositoryImpl>()
    }
}
