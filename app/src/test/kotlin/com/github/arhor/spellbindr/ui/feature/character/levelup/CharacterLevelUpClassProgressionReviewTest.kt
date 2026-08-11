package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpClassProgressionReviewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CharacterLevelUpScreen should show single-class progression and selected subclass when reviewing level-up`() {
        // Given
        val fighter = characterClass("fighter", "Fighter")
        val subclassRequirement = LevelUpRequirement.SubclassSelection(
            id = "fighter:3:subclass",
            classId = fighter.id,
            options = listOf(LevelUpChoiceOption("champion", "Champion")),
            selectedSubclassId = "champion",
        )
        val state = reviewState(
            selectedClassId = fighter.id,
            classes = listOf(fighter),
            before = snapshot(
                totalLevel = 2,
                classLevels = mapOf(fighter.id to 2),
                classDisplayName = "Fighter 2",
            ),
            after = snapshot(
                totalLevel = 3,
                classLevels = mapOf(fighter.id to 3),
                classDisplayName = "Fighter 3",
            ),
            requirements = listOf(subclassRequirement),
            selections = LevelUpSelections(subclassId = "champion"),
        )

        // When
        setContent(state)

        // Then
        composeTestRule.onNodeWithText("Class progression").assertExists()
        composeTestRule.onNodeWithText("Total level").assertExists()
        composeTestRule.onNodeWithText("Fighter level").assertExists()
        composeTestRule.onNodeWithText("Class levels").assertExists()
        assertThat(composeTestRule.onAllNodesWithText("2 → 3").fetchSemanticsNodes()).hasSize(2)
        composeTestRule.onNodeWithText("Fighter 2 → Fighter 3").assertExists()
        composeTestRule.onNodeWithText("New subclass: Champion").assertExists()
    }

    @Test
    fun `CharacterLevelUpScreen should show multiclass distribution and selected class level when reviewing multiclass level-up`() {
        // Given
        val fighter = characterClass("fighter", "Fighter")
        val wizard = characterClass("wizard", "Wizard")
        val state = reviewState(
            selectedClassId = wizard.id,
            classes = listOf(fighter, wizard),
            before = snapshot(
                totalLevel = 4,
                classLevels = mapOf(fighter.id to 3, wizard.id to 1),
                classDisplayName = "Fighter 3 / Wizard 1",
            ),
            after = snapshot(
                totalLevel = 5,
                classLevels = mapOf(fighter.id to 3, wizard.id to 2),
                classDisplayName = "Fighter 3 / Wizard 2",
            ),
        )

        // When
        setContent(state)

        // Then
        composeTestRule.onNodeWithText("4 → 5").assertExists()
        composeTestRule.onNodeWithText("Wizard level").assertExists()
        assertThat(composeTestRule.onAllNodesWithText("1 → 2").fetchSemanticsNodes()).isNotEmpty()
        composeTestRule.onNodeWithText("Fighter 3 / Wizard 1 → Fighter 3 / Wizard 2").assertExists()
        composeTestRule.onNodeWithText("New subclass", substring = true).assertDoesNotExist()
    }

    @Test
    fun `CharacterLevelUpScreen should show only changed shared spell slots for single-class level-up`() {
        val wizard = characterClass("wizard", "Wizard")
        setContent(
            reviewState(
                selectedClassId = wizard.id,
                classes = listOf(wizard),
                before = snapshot(
                    totalLevel = 4,
                    classLevels = mapOf(wizard.id to 4),
                    classDisplayName = "Wizard 4",
                    sharedSpellSlots = mapOf(1 to 4, 2 to 3),
                ),
                after = snapshot(
                    totalLevel = 5,
                    classLevels = mapOf(wizard.id to 5),
                    classDisplayName = "Wizard 5",
                    sharedSpellSlots = mapOf(1 to 4, 2 to 3, 3 to 2),
                ),
            ),
        )

        composeTestRule.onNodeWithText("Spell slots").assertExists()
        composeTestRule.onNodeWithText("3rd-level shared slots").assertExists()
        composeTestRule.onNodeWithText("1st-level shared slots").assertDoesNotExist()
        composeTestRule.onNodeWithText("2nd-level shared slots").assertDoesNotExist()
        composeTestRule.onNodeWithText("0 → 2").assertExists()
    }

    @Test
    fun `CharacterLevelUpScreen should show changed shared spell slots for multiclass level-up`() {
        val cleric = characterClass("cleric", "Cleric")
        val wizard = characterClass("wizard", "Wizard")
        setContent(
            reviewState(
                selectedClassId = wizard.id,
                classes = listOf(cleric, wizard),
                before = snapshot(
                    totalLevel = 2,
                    classLevels = mapOf(cleric.id to 1, wizard.id to 1),
                    classDisplayName = "Cleric 1 / Wizard 1",
                    sharedSpellSlots = mapOf(1 to 3),
                ),
                after = snapshot(
                    totalLevel = 3,
                    classLevels = mapOf(cleric.id to 1, wizard.id to 2),
                    classDisplayName = "Cleric 1 / Wizard 2",
                    sharedSpellSlots = mapOf(1 to 3, 2 to 2),
                ),
            ),
        )

        composeTestRule.onNodeWithText("2nd-level shared slots").assertExists()
        composeTestRule.onNodeWithText("1st-level shared slots").assertDoesNotExist()
        composeTestRule.onNodeWithText("0 → 2").assertExists()
    }

    @Test
    fun `CharacterLevelUpScreen should show Pact Magic count and level separately`() {
        val warlock = characterClass("warlock", "Warlock")
        setContent(
            reviewState(
                selectedClassId = warlock.id,
                classes = listOf(warlock),
                before = snapshot(
                    totalLevel = 1,
                    classLevels = mapOf(warlock.id to 1),
                    classDisplayName = "Warlock 1",
                    pactMagic = com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity(1, 1),
                ),
                after = snapshot(
                    totalLevel = 2,
                    classLevels = mapOf(warlock.id to 2),
                    classDisplayName = "Warlock 2",
                    pactMagic = com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity(1, 2),
                ),
            ),
        )

        composeTestRule.onNodeWithText("Pact Magic").assertExists()
        composeTestRule.onNodeWithText("Pact Magic slots").assertExists()
        assertThat(composeTestRule.onAllNodesWithText("1 → 2").fetchSemanticsNodes()).isNotEmpty()
        composeTestRule.onNodeWithText("Pact Magic slot level").assertDoesNotExist()
    }

    private fun setContent(state: CharacterLevelUpUiState.Content) {
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpScreen(
                    state = state,
                    dispatch = {},
                )
            }
        }
    }

    private fun reviewState(
        selectedClassId: String,
        classes: List<CharacterClass>,
        before: LevelUpSnapshot,
        after: LevelUpSnapshot,
        requirements: List<LevelUpRequirement> = emptyList(),
        selections: LevelUpSelections = LevelUpSelections(),
    ) = CharacterLevelUpUiState.Content(
        characterName = "Test hero",
        plan = LevelUpPlan(
            expectedTotalLevel = before.totalLevel,
            rulesetId = "srd-5e-2014-v1",
            referenceDataVersion = "test-v1",
            selectedClassId = selectedClassId,
            selections = selections,
        ),
        preview = LevelUpPreview(
            before = before,
            after = after,
            requirements = requirements,
            validations = emptyList(),
        ),
        classes = classes,
        feats = emptyList(),
        spells = emptyList(),
        steps = listOf(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review),
        step = CharacterLevelUpStep.Review,
        currentStepIndex = 1,
    )

    private fun snapshot(
        totalLevel: Int,
        classLevels: Map<String, Int>,
        classDisplayName: String,
        sharedSpellSlots: Map<Int, Int> = emptyMap(),
        pactMagic: com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity? = null,
    ) = LevelUpSnapshot(
        totalLevel = totalLevel,
        classLevels = classLevels,
        classDisplayName = classDisplayName,
        proficiencyBonus = 2,
        abilityScores = AbilityScores(),
        maximumHitPoints = 24,
        hitDicePools = emptyList(),
        proficiencyIds = emptySet(),
        savingThrowAbilityIds = emptySet(),
        featureIds = emptySet(),
        sharedCasterLevel = 0,
        sharedSpellSlots = sharedSpellSlots,
        pactMagic = pactMagic,
    )

    private fun characterClass(id: String, name: String) = CharacterClass(
        id = id,
        name = name,
        hitDie = 10,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )
}
