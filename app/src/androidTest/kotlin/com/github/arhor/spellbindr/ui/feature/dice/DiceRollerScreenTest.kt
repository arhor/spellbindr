package com.github.arhor.spellbindr.ui.feature.dice

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerUiState
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

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
                    onToggleCheck = {},
                    onCheckModeSelected = {},
                    onIncrementCheckModifier = {},
                    onDecrementCheckModifier = {},
                    onAddAmountDie = {},
                    onIncrementAmountDie = {},
                    onDecrementAmountDie = {},
                    onClearAll = {},
                    onRollMain = {},
                    onRollPercentile = {},
                    onReRollLast = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Dice Roll").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Tap d4, d6, d8, d10 or d12 above to add dice.")
            .assertIsDisplayed()
    }
}
