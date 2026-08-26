package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.AppSettings
import io.github.arhor.dnd.companion.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode?)
}
