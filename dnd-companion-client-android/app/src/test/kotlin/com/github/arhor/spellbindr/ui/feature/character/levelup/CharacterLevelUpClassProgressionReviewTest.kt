package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
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
        composeTestRule.onNodeWithText("1 → 2").assertExists()
        composeTestRule.onNodeWithText("Fighter 3 / Wizard 1 → Fighter 3 / Wizard 2").assertExists()
        composeTestRule.onNodeWithText("New subclass", substring = true).assertDoesNotExist()
    }

    @Test
    fun `CharacterLevelUpScreen should show spell changes grouped by owner and provenance`() {
        val wizard = characterClass("wizard", "Wizard")
        val cleric = characterClass("cleric", "Cleric")
        val state = reviewState(
            selectedClassId = wizard.id,
            classes = listOf(wizard, cleric),
            before = snapshot(1, mapOf("wizard" to 1), "Wizard 1"),
            after = snapshot(2, mapOf("wizard" to 2), "Wizard 2"),
            spells = listOf(spell("light", "Light", 0), spell("shield", "Shield", 1), spell("cure", "Cure Wounds", 1)),
            selections = LevelUpSelections(
                spellChanges = SpellChanges(
                    learned = setOf(ClassSpellRef("wizard", "light")),
                    featureLearned = mapOf("blessed" to setOf(ClassSpellRef("cleric", "cure"))),
                    replaced = setOf(SpellReplacement("wizard", "shield", "cure")),
                ),
            ),
        )

        setContent(state)

        composeTestRule.onNodeWithText("Spell changes").assertExists()
        composeTestRule.onNodeWithText("Wizard").assertExists()
        composeTestRule.onNodeWithText("Learned cantrip: Light (cantrip)").assertExists()
        composeTestRule.onNodeWithText("Replaced: Shield → Cure Wounds").assertExists()
        composeTestRule.onNodeWithText("Cleric").assertExists()
        composeTestRule.onNodeWithText("Granted automatically: Cure Wounds (level 1)").assertExists()
    }

    @Test
    fun `CharacterLevelUpScreen should omit spell changes when none are selected`() {
        val wizard = characterClass("wizard", "Wizard")
        val state = reviewState(
            selectedClassId = wizard.id,
            classes = listOf(wizard),
            before = snapshot(1, mapOf("wizard" to 1), "Wizard 1"),
            after = snapshot(2, mapOf("wizard" to 2), "Wizard 2"),
        )

        setContent(state)

        composeTestRule.onNodeWithText("Spell changes").assertDoesNotExist()
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
        spells: List<Spell> = emptyList(),
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
        spells = spells,
        steps = listOf(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review),
        step = CharacterLevelUpStep.Review,
        currentStepIndex = 1,
    )

    private fun snapshot(
        totalLevel: Int,
        classLevels: Map<String, Int>,
        classDisplayName: String,
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
        sharedSpellSlots = emptyMap(),
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

    private fun spell(id: String, name: String, level: Int) = Spell(
        id = id,
        name = name,
        desc = emptyList(),
        level = level,
        range = "Self",
        ritual = false,
        school = com.github.arhor.spellbindr.domain.model.EntityRef("evocation"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = emptyList(),
        components = emptyList(),
        concentration = false,
        source = "test",
    )
}
