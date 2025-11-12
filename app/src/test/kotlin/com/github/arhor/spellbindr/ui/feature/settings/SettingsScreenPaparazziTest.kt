package com.github.arhor.spellbindr.ui.feature.settings

import app.cash.paparazzi.Paparazzi
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private val defaultState = SettingsUiState(
        themeMode = AppThemeMode.DARK,
        loaded = true,
    )

    @Test
    fun settingsScreen_lightTheme() {
        paparazzi.snapshot(name = "SettingsScreen_light") {
            AppTheme(isDarkTheme = false) {
                SettingsScreen(
                    state = defaultState,
                    isDarkTheme = false,
                    onThemeToggle = {},
                )
            }
        }
    }

    @Test
    fun settingsScreen_darkTheme() {
        paparazzi.snapshot(name = "SettingsScreen_dark") {
            AppTheme(isDarkTheme = true) {
                SettingsScreen(
                    state = defaultState,
                    isDarkTheme = true,
                    onThemeToggle = {},
                )
            }
        }
    }
}
