package com.github.arhor.spellbindr.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.di.AppModule
import com.github.arhor.spellbindr.di.DatabaseModule
import com.google.common.truth.Truth.assertThat
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
    fun `opening spell should keep top bar visible when navigating to details`() {
        // Given
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Spells")
        composeTestRule.onNodeWithText("Spells").performClick()

        waitForText("Search spell by name")
        composeTestRule.onNodeWithText("Search spell by name").performTextInput("Magic Missile")
        waitForText("Magic Missile")

        // When
        composeTestRule.onNodeWithText("Magic Missile").performClick()
        waitForContentDescription("Back")
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun `dice roll details should dismiss when navigating away and returning`() {
        // Given
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Dice").performClick()

        waitForText("Dice Roll")
        composeTestRule.onNodeWithText("d4").performClick()
        composeTestRule.onNodeWithText("Roll").performClick()

        waitForText("Latest Roll")
        composeTestRule.onNodeWithText("Details").performClick()
        waitForText("Roll details")

        // When
        composeTestRule.onNodeWithText("Compendium").performClick()
        composeTestRule.onNodeWithText("Dice").performClick()

        // Then
        waitForText("Dice Roll")
        composeTestRule.onAllNodesWithText("Roll details").assertCountEquals(0)
    }

    @Test
    fun `character editor should reset when switching tabs`() {
        // Given
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithContentDescription("Create character").performClick()

        waitForText("New Character")
        val backVisible = composeTestRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()

        // When
        composeTestRule.onNodeWithText("Characters").performClick()

        waitForContentDescription("Create character")
        val editorNodes = composeTestRule.onAllNodesWithText("New Character").fetchSemanticsNodes()

        // Then
        assertThat(backVisible).isTrue()
        assertThat(editorNodes).isEmpty()
    }

    @Test
    fun `character sheet navigation should remain stable when adding spells`() {
        // Given
        waitForContentDescription("Create character")
        createCharacter("Test Hero")

        // When
        composeTestRule.onNodeWithText("Test Hero").performClick()
        waitForContentDescription("Back")
        val backVisibleDuringSheet = composeTestRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes().isNotEmpty()

        composeTestRule.onNodeWithText("Spells").performClick()
        composeTestRule.onNodeWithText("Add spells").performClick()
        waitForText("Add Spells")

        composeTestRule.onNodeWithText("Characters").performClick()
        waitForContentDescription("Create character")
        val backButtons = composeTestRule.onAllNodesWithContentDescription("Back").fetchSemanticsNodes()

        // Then
        assertThat(backVisibleDuringSheet).isTrue()
        assertThat(backButtons).isEmpty()
    }

    @Test
    fun `compendium sections should open conditions and render description`() {
        // Given
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Conditions")
        composeTestRule.onNodeWithText("Conditions").performClick()

        // When
        waitForText("Blinded")
        composeTestRule.onNodeWithText("Blinded").performClick()

        // Then
        waitForText("- A blinded creature can't see and automatically fails any ability check that requires sight.")
        composeTestRule.onNodeWithText("- A blinded creature can't see and automatically fails any ability check that requires sight.").assertIsDisplayed()
    }

    @Test
    fun `compendium sections should reappear when returning from other tabs`() {
        // Given
        waitForContentDescription("Create character")
        composeTestRule.onNodeWithText("Compendium").performClick()

        waitForText("Spells")
        composeTestRule.onNodeWithText("Spells").performClick()
        waitForText("Search spell by name")
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        waitForText("Spells")

        // When
        composeTestRule.onNodeWithText("Dice").performClick()
        composeTestRule.onNodeWithText("Compendium").performClick()

        // Then
        waitForText("Spells")
        composeTestRule.onNodeWithText("Equipment").assertIsDisplayed()
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
