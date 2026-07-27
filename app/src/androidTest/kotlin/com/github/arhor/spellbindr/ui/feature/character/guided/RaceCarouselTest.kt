package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.ui.feature.character.guided.components.race.RaceCarousel
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RaceCarouselTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun first_visible_page_is_not_selected_automatically() {
        val selectedRaceIds = mutableListOf<String>()

        composeTestRule.setContent {
            AppTheme {
                RaceCarousel(
                    races = races,
                    traitsById = emptyMap(),
                    selectedRaceId = null,
                    selectedSubraceId = null,
                    onRaceSelected = selectedRaceIds::add,
                    onSubraceSelected = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Elf, 1 of 2").assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertThat(selectedRaceIds).isEmpty()
        }
    }

    @Test
    fun tapping_visible_page_selects_exactly_that_race() {
        val selectedRaceIds = mutableListOf<String>()

        composeTestRule.setContent {
            AppTheme {
                RaceCarousel(
                    races = races,
                    traitsById = emptyMap(),
                    selectedRaceId = null,
                    selectedSubraceId = null,
                    onRaceSelected = selectedRaceIds::add,
                    onSubraceSelected = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Elf, 1 of 2").performClick()
        composeTestRule.runOnIdle {
            assertThat(selectedRaceIds).containsExactly("elf")
        }
    }

    @Test
    fun settling_a_user_swipe_selects_the_new_page_once() {
        val selectedRaceIds = mutableListOf<String>()

        composeTestRule.setContent {
            AppTheme {
                RaceCarousel(
                    races = races,
                    traitsById = emptyMap(),
                    selectedRaceId = null,
                    selectedSubraceId = null,
                    onRaceSelected = selectedRaceIds::add,
                    onSubraceSelected = { _, _ -> },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Elf, 1 of 2")
            .performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertThat(selectedRaceIds).containsExactly("dwarf")
        }
    }

    private val races = listOf(
        Race(id = "elf", name = "Elf", traits = emptyList(), subraces = emptyList()),
        Race(id = "dwarf", name = "Dwarf", traits = emptyList(), subraces = emptyList()),
    )
}
