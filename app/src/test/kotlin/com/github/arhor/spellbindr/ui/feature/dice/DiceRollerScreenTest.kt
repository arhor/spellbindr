package com.github.arhor.spellbindr.ui.feature.dice

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiceRollerScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `DiceRollerScreen should show prompt when no dice are selected`() {
        // Given
        val state = DiceRollerUiState.Content()

        // When
        composeTestRule.setContent {
            AppTheme {
                DiceRollerScreen(
                    state = state,
                    dispatch = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Dice Roll").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Tap d4, d6, d8, d10 or d12 above to add dice.")
            .assertIsDisplayed()
    }

    @Test
    fun `DiceRollerScreen should dispatch add amount die intent when d4 tapped`() {
        // Given
        val state = DiceRollerUiState.Content()
        var capturedIntent: DiceRollerIntent? = null

        // When
        composeTestRule.setContent {
            AppTheme {
                DiceRollerScreen(
                    state = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("d4").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(DiceRollerIntent.AddAmountDie(4))
        }
    }
}
