package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConditionsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `render should show loading indicator when state is loading`() {
        // Given
        val state = ConditionsUiState.Loading

        // When
        composeTestRule.setContent {
            AppTheme {
                ConditionsScreen(
                    uiState = state,
                )
            }
        }

        // Then
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun `render should show error message when state is error`() {
        // Given
        val state = ConditionsUiState.Failure("Something went wrong")

        // When
        composeTestRule.setContent {
            AppTheme {
                ConditionsScreen(
                    uiState = state,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun `render should show expanded description when selected item id matches`() {
        // Given
        val condition = Condition(
            id = "blinded",
            displayName = "Blinded",
            description = listOf("Cannot see"),
        )
        val state = ConditionsUiState.Content(
            conditions = listOf(condition),
            selectedItemId = condition.id,
        )

        // When
        composeTestRule.setContent {
            AppTheme {
                ConditionsScreen(
                    uiState = state,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Cannot see").assertIsDisplayed()
    }

    @Test
    fun `click should dispatch intent when condition item tapped`() {
        // Given
        val condition = Condition(
            id = "blinded",
            displayName = "Blinded",
            description = listOf("Cannot see"),
        )
        var capturedIntent: ConditionsIntent? = null
        val state = ConditionsUiState.Content(
            conditions = listOf(condition),
        )

        // When
        composeTestRule.setContent {
            AppTheme {
                ConditionsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("Blinded").performClick()

        // Then
        assertThat(capturedIntent).isEqualTo(ConditionsIntent.ConditionClicked(condition.id))
    }
}
