package io.github.arhor.dnd.companion.ui.feature.settings

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.AppSettings

sealed interface SettingsUiState {

    @Immutable
    data object Loading : SettingsUiState

    @Immutable
    data class Content(
        val settings: AppSettings = AppSettings(),
    ) : SettingsUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : SettingsUiState
}
