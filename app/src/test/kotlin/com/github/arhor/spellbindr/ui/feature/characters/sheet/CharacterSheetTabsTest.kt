package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CharacterSheetTabsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingWeaponsSelectsWeaponsContent() {
        setContent()

        selectTab("Weapons")

        composeTestRule.onNodeWithText("Longsword").assertIsDisplayed()
        composeTestRule.onNodeWithText("Magic Missile").assertIsNotDisplayed()
    }

    @Test
    fun rapidTabSwitchingKeepsContentAligned() {
        setContent()

        selectTab("Spells")
        composeTestRule.onNodeWithText("Magic Missile")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Longsword").assertIsNotDisplayed()

        selectTab("Weapons")
        composeTestRule.onNodeWithText("Longsword").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saving Throws").assertIsNotDisplayed()

        selectTab("Overview")
        composeTestRule.onNodeWithText("AC").assertIsDisplayed()
        composeTestRule.onNodeWithText("Longsword").assertIsNotDisplayed()

        selectTab("Skills")
        composeTestRule.onNodeWithText("Acrobatics")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Saving Throws").assertIsNotDisplayed()
    }

    private fun setContent() {
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.setContent {
            AppTheme {
                var uiState by remember { mutableStateOf(CharacterSheetPreviewData.uiState) }
                CharacterSheetContent(
                    state = uiState,
                    header = CharacterSheetPreviewData.header,
                    callbacks = CharacterSheetCallbacks(
                        onTabSelected = { tab ->
                            uiState = uiState.copy(selectedTab = tab)
                        },
                    ),
                )
            }
        }
    }

    private fun selectTab(label: String) {
        composeTestRule.onNodeWithTag("CharacterSheetTab-$label")
            .performClick()
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.waitForIdle()
    }
}
