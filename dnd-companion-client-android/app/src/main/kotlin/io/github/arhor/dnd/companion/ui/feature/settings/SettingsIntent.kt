package io.github.arhor.dnd.companion.ui.feature.settings

import io.github.arhor.dnd.companion.domain.model.ThemeMode

/**
 * Represents user intents for the Settings screen.
 */
sealed interface SettingsIntent {
    /**
     * Intent emitted when the user selects a theme mode option.
     */
    data class ThemeModeSelected(val mode: ThemeMode?) : SettingsIntent
}

/**
 * Dispatch function for [SettingsIntent] events.
 */
typealias SettingsDispatch = (SettingsIntent) -> Unit
