package com.github.arhor.spellbindr.ui.feature.character

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CharacterSheetContent should show overview tab and saving throws section when rendered`() {
        // Given
        composeTestRule.setContent {
            AppTheme {
                CharacterSheetContent(
                    state = CharacterSheetPreviewData.uiState,
                    header = CharacterSheetPreviewData.header,
                    onTabSelected = {},
                    onAddSpellsClick = {},
                    onSpellSelected = {},
                    onSpellRemoved = { _, _ -> },
                    onCastSpellClick = {},
                    onLongRestClick = {},
                    onShortRestClick = {},
                    onConfigureSlotsClick = {},
                    onSpellSlotToggle = { _, _ -> },
                    onSpellSlotTotalChanged = { _, _ -> },
                    onAddWeaponClick = {},
                    onWeaponSelected = {},
                    onPactSlotToggle = {},
                    onPactSlotTotalChanged = {},
                    onPactSlotLevelChanged = {},
                    onConcentrationClear = {},
                )
            }
        }

        // When
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saving Throws").assertIsDisplayed()
    }
}
