package com.github.arhor.spellbindr.ui.feature.character

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.feature.character.list.CharactersListScreen
import com.github.arhor.spellbindr.ui.feature.character.list.CharactersListUiState
import com.github.arhor.spellbindr.ui.feature.character.list.model.CharacterListItem
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharactersListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CharactersListScreen should show derived character details when state contains character`() {
        // Given
        val uiState = CharactersListUiState.Content(
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
        )

        // When
        composeTestRule.setContent {
            AppTheme {
                CharactersListScreen(
                    state = uiState,
                    onCharacterSelected = {},
                    onCreateCharacter = { _ -> },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Unnamed hero").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 2 Rogue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elf").assertIsDisplayed()
    }
}
