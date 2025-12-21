package com.github.arhor.spellbindr.ui.feature.dice

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.arhor.spellbindr.ui.feature.dice.model.DiceRollerState
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
    fun showsPromptWhenNoDiceSelected() {
        val state = DiceRollerState()

        composeTestRule.setContent {
            AppTheme {
                DiceRollerScreen(
                    state = state,
                    onIntent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Dice Roll").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Tap d4, d6, d8, d10 or d12 above to add dice.")
            .assertIsDisplayed()
    }
}
