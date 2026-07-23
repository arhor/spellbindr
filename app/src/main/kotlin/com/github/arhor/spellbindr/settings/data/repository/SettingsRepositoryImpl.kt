package com.github.arhor.spellbindr.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.SettingsRepository
import com.github.arhor.spellbindr.logging.LoggerFactory
import com.github.arhor.spellbindr.logging.error
import com.github.arhor.spellbindr.logging.getLogger
import com.github.arhor.spellbindr.settings.di.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>,
    loggerFactory: LoggerFactory,
) : SettingsRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val logger = loggerFactory.getLogger()

    override val settings: Flow<AppSettings>
        get() = dataStore.data.map { preferences ->
            val themeMode = preferences[themeModeKey]?.let { storedValue ->
                runCatching { ThemeMode.valueOf(storedValue) }
                    .getOrElse { error ->
                        logger.error(error) { "Invalid theme mode stored: $storedValue" }
                        null
                    }
            }

            AppSettings(themeMode = themeMode)
        }

    override suspend fun setThemeMode(mode: ThemeMode?) {
        dataStore.edit { preferences ->
            if (mode == null) {
                preferences.remove(themeModeKey)
            } else {
                preferences[themeModeKey] = mode.name
            }
        }
    }
}
