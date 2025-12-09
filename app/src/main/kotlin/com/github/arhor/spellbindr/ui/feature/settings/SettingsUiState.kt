package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.data.model.AppThemeMode

@Immutable
data class SettingsUiState(
    val themeMode: AppThemeMode? = null,
    val loaded: Boolean = false,
) {
    val isDarkTheme: Boolean?
        get() = themeMode?.isDark
}
