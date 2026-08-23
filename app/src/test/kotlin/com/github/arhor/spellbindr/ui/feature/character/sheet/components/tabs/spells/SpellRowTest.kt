package com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpellRowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `SpellRow should keep cast action independent from card click`() {
        // Given
        var cardClicks = 0
        var castClicks = 0
        composeTestRule.setContent {
            AppTheme {
                SpellRow(
                    spell = testSpell,
                    editMode = SheetEditMode.View,
                    onClick = { cardClicks += 1 },
                    canCast = true,
                    onCastClick = { castClicks += 1 },
                    onRemove = {},
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Cast").performClick()

        // Then
        assertThat(castClicks).isEqualTo(1)
        assertThat(cardClicks).isEqualTo(0)
    }

    @Test
    fun `SpellRow should expose compact mechanical metadata and disabled cast state`() {
        // Given
        composeTestRule.setContent {
            AppTheme {
                SpellRow(
                    spell = testSpell,
                    editMode = SheetEditMode.View,
                    onClick = {},
                    canCast = false,
                    onCastClick = {},
                    onRemove = {},
                )
            }
        }

        // When / Then
        composeTestRule.onNodeWithText("Evocation · Action · 120 ft").assertExists()
        composeTestRule.onNodeWithText("VSM").assertExists()
        composeTestRule.onNodeWithText("Concentration").assertExists()
        composeTestRule.onNodeWithText("Ritual").assertExists()
        composeTestRule.onNodeWithText("Cast").assertIsNotEnabled()
    }

    @Test
    fun `SpellRow should keep remove action independent from card click`() {
        // Given
        var cardClicks = 0
        var removeClicks = 0
        composeTestRule.setContent {
            AppTheme {
                SpellRow(
                    spell = testSpell,
                    editMode = SheetEditMode.Edit,
                    onClick = { cardClicks += 1 },
                    canCast = true,
                    onCastClick = {},
                    onRemove = { removeClicks += 1 },
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Remove spell").performClick()

        // Then
        assertThat(removeClicks).isEqualTo(1)
        assertThat(cardClicks).isEqualTo(0)
    }
}

private val testSpell = CharacterSpellUiModel(
    spellId = "fire_bolt",
    name = "Fire Bolt",
    level = 0,
    school = "Evocation",
    castingTime = "Action",
    range = "120 ft",
    components = listOf("V", "S", "M"),
    ritual = true,
    concentration = true,
    sourceClass = "Wizard",
    sourceLabel = "Wizard",
    sourceKey = "wizard",
)
