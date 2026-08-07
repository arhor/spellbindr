package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpRetryScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `retryable failure should show retry and dispatch retry intent`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpRouteContent(
                    state = CharacterLevelUpUiState.Failure(
                        message = "Unable to load level-up reference data",
                        canRetry = true,
                    ),
                    dispatch = intents::add,
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        assertThat(intents).containsExactly(CharacterLevelUpIntent.RetryClicked)
    }

    @Test
    fun `terminal failure should not show retry`() {
        // Given
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpRouteContent(
                    state = CharacterLevelUpUiState.Failure("Character not found"),
                    dispatch = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
    }
}
