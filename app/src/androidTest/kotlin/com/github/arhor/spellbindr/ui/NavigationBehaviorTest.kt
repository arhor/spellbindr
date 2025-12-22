package com.github.arhor.spellbindr.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.MainActivity
import com.github.arhor.spellbindr.di.AppModule
import com.github.arhor.spellbindr.di.DatabaseModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(AppModule::class, DatabaseModule::class)
@RunWith(AndroidJUnit4::class)
class NavigationBehaviorTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun openingSpellKeepsTopBarVisible() {
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Search spell by name")
        composeTestRule.onNodeWithText("Search spell by name").performTextInput("Magic Missile")
        waitForText("Magic Missile")
        composeTestRule.onNodeWithText("Magic Missile").performClick()

        waitForContentDescription("Back")
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun diceDetailsDismissAfterNavigation() {
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Dice").performClick()

        waitForText("Dice Roll")
        composeTestRule.onNodeWithText("d4").performClick()
        composeTestRule.onNodeWithText("Roll").performClick()

        waitForText("Latest Roll")
        composeTestRule.onNodeWithText("Details").performClick()
        waitForText("Roll details")

        composeTestRule.onNodeWithText("Compendium").performClick()
        composeTestRule.onNodeWithText("Dice").performClick()

        waitForText("Dice Roll")
        composeTestRule.onAllNodesWithText("Roll details").assertCountEquals(0)
    }

    @Test
    fun characterEditorResetsWhenSwitchingTabs() {
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithContentDescription("Create character").performClick()

        waitForText("New Character")
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        composeTestRule.onNodeWithText("Characters").performClick()

        waitForContentDescription("Create character")
        composeTestRule.onAllNodesWithText("New Character").assertCountEquals(0)
    }

    @Test
    fun characterSheetNavigationAndAddSpellsAreStable() {
        waitForContentDescription("Create character")
        createCharacter("Test Hero")

        composeTestRule.onNodeWithText("Test Hero").performClick()
        waitForContentDescription("Back")
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        composeTestRule.onNodeWithText("Spells").performClick()
        composeTestRule.onNodeWithText("Add spells").performClick()
        waitForText("Add Spells")

        composeTestRule.onNodeWithText("Characters").performClick()
        waitForContentDescription("Create character")
        composeTestRule.onAllNodesWithContentDescription("Back").assertCountEquals(0)
    }

    @Test
    fun compendiumTabResetsOnTopLevelSwitch() {
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Search spell by name")
        composeTestRule.onNodeWithText("Races").performClick()

        composeTestRule.onNodeWithText("Dice").performClick()
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Search spell by name")
        composeTestRule.onNodeWithText("Search spell by name").assertIsDisplayed()
    }

    private fun createCharacter(name: String) {
        composeTestRule.onNodeWithContentDescription("Create character").performClick()
        waitForText("Name")
        composeTestRule.onNodeWithText("Name").performTextInput(name)
        composeTestRule.onNodeWithText("Save").performClick()
        waitForText(name)
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000L) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForContentDescription(description: String, timeoutMillis: Long = 10_000L) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithContentDescription(description, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
