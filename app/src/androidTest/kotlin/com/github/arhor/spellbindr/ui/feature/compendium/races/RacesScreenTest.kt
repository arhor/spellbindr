package com.github.arhor.spellbindr.ui.feature.compendium.races

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RacesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `render should show loading indicator when state is loading`() {
        // Given
        val state = RacesUiState.Loading

        // When
        composeTestRule.setContent {
            AppTheme {
                RacesScreen(
                    state = state,
                )
            }
        }

        // Then
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `render should show error message when state is error`() {
        // Given
        val state = RacesUiState.Error("Something went wrong")

        // When
        composeTestRule.setContent {
            AppTheme {
                RacesScreen(
                    state = state,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
    }

    @Test
    fun `render should show trait description when selected item id matches`() {
        // Given
        val trait = Trait(
            id = "darkvision",
            name = "Darkvision",
            desc = listOf("Can see in dim light."),
        )
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef(trait.id)),
            subraces = emptyList(),
        )
        val state = RacesUiState.Content(
            races = listOf(race),
            traits = mapOf(trait.id to trait),
            selectedItemId = race.id,
        )

        // When
        composeTestRule.setContent {
            AppTheme {
                RacesScreen(
                    state = state,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Can see in dim light.").assertIsDisplayed()
    }

    @Test
    fun `click should call onRaceClick when race item tapped`() {
        // Given
        val trait = Trait(
            id = "keen_senses",
            name = "Keen Senses",
            desc = listOf("Proficiency in Perception."),
        )
        val race = Race(
            id = "elf",
            name = "Elf",
            traits = listOf(EntityRef(trait.id)),
            subraces = emptyList(),
        )
        val state = RacesUiState.Content(
            races = listOf(race),
            traits = mapOf(trait.id to trait),
        )
        var clickedRace: Race? = null

        // When
        composeTestRule.setContent {
            AppTheme {
                RacesScreen(
                    state = state,
                    onRaceClick = { clickedRace = it },
                )
            }
        }
        composeTestRule.onNodeWithText("Elf").performClick()

        // Then
        assertThat(clickedRace).isEqualTo(race)
    }
}
