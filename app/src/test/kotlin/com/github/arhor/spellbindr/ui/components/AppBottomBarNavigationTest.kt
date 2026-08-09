package com.github.arhor.spellbindr.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppBottomBarNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var controller: NavHostController

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AppTheme {
                NavigationHarness(onControllerReady = { controller = it })
            }
        }
    }

    @Test
    fun `AppBottomBar should discard character editor when characters tab is selected`() {
        // Given
        composeTestRule.runOnIdle {
            controller.navigate(AppDestination.CharacterEditor())
        }

        // When
        composeTestRule.onNodeWithText("Characters").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(controller.currentDestination!!.hasRoute<AppDestination.CharactersHome>()).isTrue()
            assertThat(controller.previousBackStackEntry).isNull()
        }
    }

    @Test
    fun `AppBottomBar should discard character sheet stack when characters tab is selected from spell picker`() {
        // Given
        composeTestRule.runOnIdle {
            controller.navigate(AppDestination.CharacterSheet(characterId = "character-id"))
            controller.navigate(AppDestination.CharacterSpellPicker(characterId = "character-id"))
        }

        // When
        composeTestRule.onNodeWithText("Characters").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(controller.currentDestination!!.hasRoute<AppDestination.CharactersHome>()).isTrue()
            assertThat(controller.previousBackStackEntry).isNull()
        }
    }

    @Test
    fun `AppBottomBar should create fresh dice destination when returning from another tab`() {
        // Given
        composeTestRule.onNodeWithText("Dice").performClick()
        lateinit var firstDiceEntryId: String
        composeTestRule.runOnIdle {
            firstDiceEntryId = controller.currentBackStackEntry!!.id
        }

        // When
        composeTestRule.onNodeWithText("Compendium").performClick()
        composeTestRule.onNodeWithText("Dice").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(controller.currentDestination!!.hasRoute<AppDestination.Dice>()).isTrue()
            assertThat(controller.currentBackStackEntry!!.id).isNotEqualTo(firstDiceEntryId)
        }
    }

    @Test
    fun `AppBottomBar should return to compendium root when compendium tab is selected`() {
        // Given
        composeTestRule.runOnIdle {
            controller.navigate(AppDestination.CompendiumSections)
            controller.navigate(AppDestination.Spells)
        }

        // When
        composeTestRule.onNodeWithText("Dice").performClick()
        composeTestRule.onNodeWithText("Compendium").performClick()

        // Then
        composeTestRule.runOnIdle {
            assertThat(controller.currentDestination!!.hasRoute<AppDestination.CompendiumSections>()).isTrue()
            assertThat(
                controller.previousBackStackEntry!!.destination.hasRoute<AppDestination.CharactersHome>(),
            ).isTrue()
        }
    }
}

/**
 * A deliberately small graph keeps Robolectric synchronization deterministic. The former full-activity tests
 * waited for Hilt, Room, asset loading, and Compose semantics at once even though these assertions only exercise
 * the bottom-navigation back-stack policy.
 */
@Composable
private fun NavigationHarness(onControllerReady: (NavHostController) -> Unit) {
    val controller = rememberNavController()
    onControllerReady(controller)

    NavHost(
        navController = controller,
        startDestination = AppDestination.CharactersHome,
    ) {
        composable<AppDestination.CharactersHome> {}
        composable<AppDestination.CharacterEditor> {}
        composable<AppDestination.CharacterSheet> {}
        composable<AppDestination.CharacterSpellPicker> {}
        composable<AppDestination.CompendiumSections> {}
        composable<AppDestination.Spells> {}
        composable<AppDestination.Dice> {}
        composable<AppDestination.Settings> {}
    }
    AppBottomBar(controller)
}
