package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpAbilityScoreReviewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `review shows changed ability score and modifiers`() {
        setContent(state(before = AbilityScores(strength = 15), after = AbilityScores(strength = 17)))

        composeTestRule.onNodeWithText("Ability scores").assertExists()
        composeTestRule.onNodeWithText("Strength").assertExists()
        composeTestRule.onNodeWithText("15 (+2) → 17 (+3)").assertExists()
        composeTestRule.onNodeWithText("Dexterity").assertDoesNotExist()
    }

    @Test
    fun `review shows ability change produced by a feat`() {
        setContent(state(before = AbilityScores(wisdom = 12), after = AbilityScores(wisdom = 13)))

        composeTestRule.onNodeWithText("Wisdom").assertExists()
        composeTestRule.onNodeWithText("12 (+1) → 13 (+1)").assertExists()
    }

    @Test
    fun `review omits ability section when no scores changed`() {
        setContent(state(before = AbilityScores(dexterity = 14), after = AbilityScores(dexterity = 14)))

        composeTestRule.onNodeWithText("Ability scores").assertDoesNotExist()
    }

    private fun setContent(state: CharacterLevelUpUiState.Content) {
        composeTestRule.setContent {
            AppTheme { CharacterLevelUpScreen(state = state, dispatch = {}) }
        }
    }

    private fun state(before: AbilityScores, after: AbilityScores) = CharacterLevelUpUiState.Content(
        characterName = "Test hero",
        plan = LevelUpPlan(1, "srd-5e-2014-v1", "test-v1", selections = LevelUpSelections()),
        preview = LevelUpPreview(snapshot(before), snapshot(after), emptyList(), emptyList()),
        classes = emptyList<CharacterClass>(),
        feats = emptyList(),
        spells = emptyList(),
        steps = listOf(CharacterLevelUpStep.Review),
        step = CharacterLevelUpStep.Review,
        currentStepIndex = 0,
    )

    private fun snapshot(abilityScores: AbilityScores) = LevelUpSnapshot(
        totalLevel = 1,
        classLevels = emptyMap(),
        classDisplayName = "Fighter 1",
        proficiencyBonus = 2,
        abilityScores = abilityScores,
        maximumHitPoints = 1,
        hitDicePools = emptyList(),
        proficiencyIds = emptySet(),
        savingThrowAbilityIds = emptySet(),
        featureIds = emptySet(),
        sharedCasterLevel = 0,
        sharedSpellSlots = emptyMap(),
    )
}
