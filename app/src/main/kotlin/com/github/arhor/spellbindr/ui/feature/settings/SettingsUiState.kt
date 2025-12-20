package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.ThemeMode

@Immutable
data class SettingsUiState(
    val themeMode: ThemeMode? = null,
    val loaded: Boolean = false,
) {
    val isDarkTheme: Boolean?
        get() = themeMode?.isDark
}
