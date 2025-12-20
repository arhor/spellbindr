package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeMode: Flow<ThemeMode?>

    suspend fun setThemeMode(mode: ThemeMode?)
}
