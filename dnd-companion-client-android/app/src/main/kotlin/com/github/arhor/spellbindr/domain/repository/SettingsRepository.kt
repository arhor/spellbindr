package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode?)
}
