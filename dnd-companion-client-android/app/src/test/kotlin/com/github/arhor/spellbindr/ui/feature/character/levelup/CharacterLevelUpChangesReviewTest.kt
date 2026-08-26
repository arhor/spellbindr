package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpChangesReviewTest {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `review shows only automatic additions grouped and selected feature option`() {
        val before = snapshot(
            proficiencyIds = setOf("skill-stealth"),
            savingThrows = emptySet(),
            languages = emptySet(),
            features = setOf("existing"),
        )
        val after = before.copy(
            proficiencyIds = before.proficiencyIds + setOf("skill-perception", "armor-shields", "weapon-martial"),
            savingThrowAbilityIds = setOf("wisdom"),
            languageIds = setOf("common"),
            featureIds = before.featureIds + "second-wind",
        )
        val requirement = LevelUpRequirement.ChoiceSelection(
            id = "second-wind:choice",
            sourceId = "second-wind",
            label = "Second Wind option",
            choice = Choice.ProficiencyChoice(choose = 1, from = listOf("tool-thieves")),
            selectedOptionIds = setOf("tool-thieves"),
            category = LevelUpChoiceCategory.Feature,
            options = listOf(LevelUpChoiceOption("tool-thieves", "Thieves' tools")),
        )
        val state = CharacterLevelUpUiState.Content(
            characterName = "Hero",
            plan = LevelUpPlan(2, "srd-5e-2014-v1", "test-v1", selectedClassId = "fighter"),
            preview = LevelUpPreview(before, after, listOf(requirement), emptyList()),
            classes = listOf(characterClass()),
            feats = emptyList(),
            spells = emptyList(),
            steps = listOf(CharacterLevelUpStep.Review),
            step = CharacterLevelUpStep.Review,
            currentStepIndex = 0,
            features = listOf(Feature("second-wind", "Second Wind", emptyList())),
        )

        composeTestRule.setContent { AppTheme { CharacterLevelUpScreen(state, {}) } }

        composeTestRule.onNodeWithText("New gains").assertExists()
        composeTestRule.onNodeWithText("Skills").assertExists()
        composeTestRule.onNodeWithText("Perception").assertExists()
        composeTestRule.onNodeWithText("Saving throws").assertExists()
        composeTestRule.onNodeWithText("WISDOM").assertExists()
        composeTestRule.onNodeWithText("Armor").assertExists()
        composeTestRule.onNodeWithText("Shields").assertExists()
        composeTestRule.onNodeWithText("Languages").assertExists()
        composeTestRule.onNodeWithText("Common").assertExists()
        composeTestRule.onNodeWithText("Class or subclass features").assertExists()
        composeTestRule.onNodeWithText("Selected feature options").assertExists()
        composeTestRule.onNodeWithText("Thieves' tools (Feature)").assertExists()
        composeTestRule.onNodeWithText("Stealth").assertDoesNotExist()
    }

    private fun snapshot(
        proficiencyIds: Set<String> = emptySet(),
        savingThrows: Set<String> = emptySet(),
        languages: Set<String> = emptySet(),
        features: Set<String> = emptySet(),
    ) = LevelUpSnapshot(
        totalLevel = 1,
        classLevels = mapOf("fighter" to 1),
        classDisplayName = "Fighter 1",
        proficiencyBonus = 2,
        abilityScores = AbilityScores(),
        maximumHitPoints = 10,
        hitDicePools = emptyList(),
        proficiencyIds = proficiencyIds,
        savingThrowAbilityIds = savingThrows,
        featureIds = features,
        sharedCasterLevel = 0,
        sharedSpellSlots = emptyMap(),
        languageIds = languages,
    )

    private fun characterClass() = CharacterClass(
        id = "fighter", name = "Fighter", hitDie = 10,
        proficiencies = emptyList(), proficiencyChoices = emptyList(), savingThrows = emptyList(),
        subclasses = emptyList(), levels = emptyList(),
    )
}
