package io.github.arhor.dnd.companion.ui.feature.settings

/**
 * Represents one-off UI effects for the Settings feature.
 */
sealed interface SettingsEffect {
    /**
     * Effect emitted to display a message to the user.
     */
    data class ShowMessage(val message: String) : SettingsEffect
}
