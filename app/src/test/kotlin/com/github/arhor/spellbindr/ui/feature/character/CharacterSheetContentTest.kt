package com.github.arhor.spellbindr.ui.feature.character

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetDispatch
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetIntent
import com.github.arhor.spellbindr.ui.feature.character.sheet.CharacterSheetScreen
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.ProgressionSummaryUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
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
        composeTestRule.onNodeWithText("Saving Throws").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `CharacterSheetScreen should dispatch level up intent when managed character is below level twenty`() {
        // Given
        val intents = mutableListOf<CharacterSheetIntent>()
        setScreenContent(
            progression = ProgressionSummaryUiModel.Managed(7, "Wizard 7", emptyList()),
            dispatch = intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Level up").assertIsEnabled().performClick()

        // Then
        assertThat(intents).containsExactly(CharacterSheetIntent.LevelUpClicked)
    }

    @Test
    fun `CharacterSheetScreen should disable level up action when managed character is level twenty`() {
        // Given
        val intents = mutableListOf<CharacterSheetIntent>()
        setScreenContent(
            progression = ProgressionSummaryUiModel.Managed(20, "Wizard 20", emptyList()),
            dispatch = intents::add,
        )

        // When
        val action = composeTestRule.onNodeWithText("Maximum level reached")

        // Then
        action.assertIsDisplayed().assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    @Test
    fun `CharacterSheetScreen should disable setup action when character progression is unmanaged`() {
        // Given
        val intents = mutableListOf<CharacterSheetIntent>()
        setScreenContent(progression = ProgressionSummaryUiModel.Unmanaged, dispatch = intents::add)

        // When
        val action = composeTestRule.onNodeWithText("Set up level progression")

        // Then
        action.assertIsDisplayed().assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    private fun setScreenContent(
        progression: ProgressionSummaryUiModel,
        dispatch: CharacterSheetDispatch,
    ) {
        composeTestRule.setContent {
            AppTheme {
                CharacterSheetScreen(
                    state = CharacterSheetPreviewData.uiState.copy(progression = progression),
                    dispatch = dispatch,
                )
            }
        }
    }
}
