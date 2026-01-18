package com.github.arhor.spellbindr.ui.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.ui.theme.AppTheme
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
        val state = SettingsUiState.Content(themeMode = ThemeMode.DARK)

        // When
        composeTestRule.setContent {
            AppTheme {
                SettingsScreen(
                    state = state,
                    onThemeModeSelected = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        composeTestRule.onNodeWithText("Always use the dark palette").assertIsDisplayed()
    }
}
