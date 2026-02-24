package com.github.arhor.spellbindr.ui.feature.character.sheet.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.feature.character.R
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.SpellsTab
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpellsTabAccessibilityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `SpellsTab exposes slot control content descriptions`() {
        composeTestRule.setContent {
            AppTheme {
                SpellsTab(
                    spellsState = CharacterSheetPreviewData.spells,
                    editMode = SheetEditMode.Edit,
                    onAddSpellsClick = {},
                    onCastSpellClick = {},
                    onLongRestClick = {},
                    onShortRestClick = {},
                    onConfigureSlotsClick = {},
                    onSpellSlotToggle = { _, _ -> },
                    onSpellSlotTotalChanged = { _, _ -> },
                    onPactSlotToggle = {},
                    onPactSlotTotalChanged = {},
                    onPactSlotLevelChanged = {},
                    onConcentrationClear = {},
                    onSpellSelected = {},
                    onSpellRemoved = { _, _ -> },
                    listState = rememberLazyListState(),
                )
            }
        }

        val context = composeTestRule.activity
        val increaseShared = context.getString(R.string.spells_shared_slots_increase, 1)
        val decreaseShared = context.getString(R.string.spells_shared_slots_decrease, 1)
        val increasePact = context.getString(R.string.spells_pact_slots_increase)
        val decreasePact = context.getString(R.string.spells_pact_slots_decrease)
        val slotTypeShared = context.getString(R.string.spells_slot_type_shared)
        val usedPip = context.getString(R.string.spells_slot_pip_used, slotTypeShared, 1, 4)

        composeTestRule.onNodeWithContentDescription(increaseShared).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(decreaseShared).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(increasePact).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(decreasePact).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(usedPip).assertIsDisplayed()
    }
}
