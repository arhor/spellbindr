package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.domain.model.AppThemeMode
import com.github.arhor.spellbindr.domain.model.ThemeMode

fun AppThemeMode.toDomain(): ThemeMode = when (this) {
    AppThemeMode.LIGHT -> ThemeMode.LIGHT
    AppThemeMode.DARK -> ThemeMode.DARK
}

fun ThemeMode.toData(): AppThemeMode = when (this) {
    ThemeMode.LIGHT -> AppThemeMode.LIGHT
    ThemeMode.DARK -> AppThemeMode.DARK
}
