package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellsTabState
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpellsTabScrollStateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `Spells tab preserves scroll position when switching tabs`() {
        val spellsState = buildSpellsState(spellCount = 60)
        composeTestRule.setContent {
            var selectedTab by remember { mutableStateOf(CharacterSheetTab.Spells) }
            val baseState = CharacterSheetPreviewData.uiState
            val state = baseState.copy(
                selectedTab = selectedTab,
                spells = spellsState,
            )

            AppTheme {
                CharacterSheetContent(
                    state = state,
                    header = CharacterSheetPreviewData.header,
                    onTabSelected = { selectedTab = it },
                    onAddSpellsClick = {},
                    onSpellSelected = {},
                    onSpellRemoved = { _, _ -> },
                    onSpellSlotToggle = { _, _ -> },
                    onSpellSlotTotalChanged = { _, _ -> },
                    onPactSlotToggle = {},
                    onPactSlotTotalChanged = {},
                    onConcentrationClear = {},
                    onAddWeaponClick = {},
                    onWeaponSelected = {},
                    modifier = androidx.compose.ui.Modifier,
                )
            }
        }

        val targetSpell = "Spell 40"
        composeTestRule.onNodeWithText(targetSpell).performScrollTo()
        composeTestRule.onNodeWithText(targetSpell).assertIsDisplayed()

        composeTestRule.onNodeWithText("Overview").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Spells").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(targetSpell).assertIsDisplayed()
    }
}

private fun buildSpellsState(spellCount: Int): SpellsTabState {
    val spells = (1..spellCount).map { index ->
        CharacterSpellUiModel(
            spellId = "spell-$index",
            name = "Spell $index",
            level = 1,
            school = "Evocation",
            castingTime = "1 action",
            sourceClass = "Wizard",
            sourceLabel = "Wizard",
            sourceKey = "wizard",
        )
    }
    val spellLevels = listOf(
        SpellLevelUiModel(
            level = 1,
            spells = spells,
        )
    )
    val classes = listOf(
        SpellcastingClassUiModel(
            sourceKey = "wizard",
            name = "Wizard",
            isUnassigned = false,
            spellcastingAbility = "INT",
            spellSaveDc = 15,
            spellAttackBonus = 7,
            spellLevels = spellLevels,
        )
    )
    return SpellsTabState(
        spellcastingClasses = classes,
        canAddSpells = true,
        sharedSlots = listOf(SpellSlotUiModel(level = 1, total = 4, expended = 0)),
        hasConfiguredSharedSlots = true,
        pactSlots = PactSlotUiModel(slotLevel = 2, total = 2, expended = 0, isConfigured = true),
        concentration = null,
    )
}
