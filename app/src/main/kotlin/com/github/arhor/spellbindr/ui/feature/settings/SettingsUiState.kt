package com.github.arhor.spellbindr.ui.feature.settings

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.ThemeMode

sealed interface SettingsUiState {

    @Immutable
    data object Loading : SettingsUiState

    @Immutable
    data class Content(
        val themeMode: ThemeMode? = null,
    ) : SettingsUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : SettingsUiState
}
