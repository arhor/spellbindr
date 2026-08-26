package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardArrayAssignTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `StandardArrayAssign should update row and progress when available score is assigned`() {
        // Given
        setContent()

        // When
        composeTestRule.onNodeWithContentDescription("15 available. Tap to select.").performClick()
        composeTestRule.onNodeWithContentDescription("Strength: unassigned. Tap to assign 15.").performClick()

        // Then
        composeTestRule.onNodeWithText("1 of 6 assigned").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Strength: 15, modifier +2. Tap to pick up 15.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription("15 available. Tap to select.").assertCountEquals(0)
    }

    @Test
    fun `StandardArrayAssign should clear row and select score when assigned score is picked up`() {
        // Given
        setContent(assignments = assignments(strength = 15))

        // When
        composeTestRule
            .onNodeWithContentDescription("Strength: 15, modifier +2. Tap to pick up 15.")
            .performClick()

        // Then
        composeTestRule.onNodeWithText("0 of 6 assigned").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("15 selected. Tap to cancel selection.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Strength: unassigned. Tap to assign 15.").assertIsDisplayed()
    }

    @Test
    fun `StandardArrayAssign should return displaced score when assigned score is replaced`() {
        // Given
        setContent(assignments = assignments(strength = 15, dexterity = 14))

        // When
        composeTestRule.onNodeWithContentDescription("13 available. Tap to select.").performClick()
        // Then
        composeTestRule
            .onNodeWithContentDescription("Strength: 15, modifier +2. Tap to assign 13.")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Strength: 13, modifier +1. Tap to pick up 13.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("15 available. Tap to select.").assertIsDisplayed()
    }

    private fun setContent(assignments: Map<String, Int?> = assignments()) {
        composeTestRule.setContent {
            var currentAssignments by remember { mutableStateOf(assignments) }
            AppTheme {
                StandardArrayAssign(
                    assignments = currentAssignments,
                    onStandardArrayAssigned = { abilityId, score ->
                        currentAssignments = currentAssignments.toMutableMap().apply { put(abilityId, score) }
                    },
                )
            }
        }
    }

    private fun assignments(
        strength: Int? = null,
        dexterity: Int? = null,
    ): Map<String, Int?> = mapOf(
        AbilityIds.STR to strength,
        AbilityIds.DEX to dexterity,
        AbilityIds.CON to null,
        AbilityIds.INT to null,
        AbilityIds.WIS to null,
        AbilityIds.CHA to null,
    )
}
