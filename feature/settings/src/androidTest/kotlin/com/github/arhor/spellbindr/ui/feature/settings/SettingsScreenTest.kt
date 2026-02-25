package com.github.arhor.spellbindr.ui.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `SettingsScreen should show selected theme option details when state is loaded`() {
        // Given
        val state = SettingsUiState.Content(settings = AppSettings(themeMode = ThemeMode.DARK))

        // When
        composeTestRule.setContent {
            AppTheme {
                SettingsScreen(
                    state = state,
                    dispatch = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        composeTestRule.onNodeWithText("Always use the dark palette").assertIsDisplayed()
    }

    @Test
    fun `SettingsScreen should dispatch intent when theme option clicked`() {
        // Given
        val state = SettingsUiState.Content(settings = AppSettings(themeMode = null))
        var capturedIntent: SettingsIntent? = null

        // When
        composeTestRule.setContent {
            AppTheme {
                SettingsScreen(
                    state = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }

        composeTestRule
            .onNodeWithText("Dark")
            .performClick()

        // Then
        assertEquals(
            SettingsIntent.ThemeModeSelected(ThemeMode.DARK),
            capturedIntent,
        )
    }
}
