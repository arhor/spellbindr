package com.github.arhor.spellbindr.ui.feature.characters

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CharactersListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsDerivedCharacterDetailsFromState() {
        val uiState = CharactersListUiState(
            characters = listOf(
                CharacterListItem(
                    id = "hero-1",
                    name = "",
                    level = 2,
                    className = "Rogue",
                    race = "Elf",
                    background = "",
                )
            ),
            isLoading = false,
            isEmpty = false,
        )

        composeTestRule.setContent {
            AppTheme {
                CharactersListScreen(
                    uiState = uiState,
                    onCharacterSelected = {},
                    onCreateCharacter = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Unnamed hero").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 2 Rogue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elf").assertIsDisplayed()
    }
}
