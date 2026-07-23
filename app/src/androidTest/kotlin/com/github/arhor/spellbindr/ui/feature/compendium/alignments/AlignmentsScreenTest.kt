package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlignmentsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `render should show loading indicator when state is loading`() {
        // Given
        val state = AlignmentsUiState.Loading

        // When
        composeTestRule.setContent {
            AppTheme {
                AlignmentsScreen(uiState = state)
            }
        }

        // Then
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun `render should show error message when state is error`() {
        // Given
        val state = AlignmentsUiState.Failure("Failed to load alignments")

        // When
        composeTestRule.setContent {
            AppTheme {
                AlignmentsScreen(uiState = state)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load alignments").assertIsDisplayed()
    }

    @Test
    fun `click should dispatch intent when alignment tile tapped`() {
        // Given
        val alignment = Alignment(
            id = "lg",
            name = "Lawful Good",
            desc = "Acts with compassion, honor, and duty.",
            abbr = "LG",
        )
        var capturedIntent: AlignmentsIntent? = null
        val state = AlignmentsUiState.Content(alignments = listOf(alignment))

        // When
        composeTestRule.setContent {
            AppTheme {
                AlignmentsScreen(
                    uiState = state,
                    dispatch = { intent -> capturedIntent = intent },
                )
            }
        }
        composeTestRule.onNodeWithText("LG").performClick()

        // Then
        composeTestRule.onNodeWithText("Lawful Good").assertIsDisplayed()
        composeTestRule.onNodeWithText("Acts with compassion, honor, and duty.").assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertThat(capturedIntent).isEqualTo(AlignmentsIntent.AlignmentClicked(alignment.id))
        }
    }
}
