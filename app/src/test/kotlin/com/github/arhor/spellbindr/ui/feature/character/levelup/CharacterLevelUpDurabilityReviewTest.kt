package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpHitDicePool
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
class CharacterLevelUpDurabilityReviewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CharacterLevelUpScreen should show fixed HP contribution and single-class hit dice when reviewing level-up`() {
        // Given
        val fighter = characterClass("fighter", "Fighter", hitDie = 10)
        val state = reviewState(
            selectedClassId = fighter.id,
            classes = listOf(fighter),
            hitPointGain = HitPointGain.Fixed(6),
            hitDie = 10,
            before = snapshot(
                totalLevel = 2,
                classLevels = mapOf(fighter.id to 2),
                classDisplayName = "Fighter 2",
                maximumHitPoints = 20,
                hitDicePools = listOf(LevelUpHitDicePool(10, 2)),
                constitution = 14,
            ),
            after = snapshot(
                totalLevel = 3,
                classLevels = mapOf(fighter.id to 3),
                classDisplayName = "Fighter 3",
                maximumHitPoints = 28,
                hitDicePools = listOf(LevelUpHitDicePool(10, 3)),
                constitution = 14,
            ),
        )

        // When
        setContent(state)

        // Then
        composeTestRule.onNodeWithText("Durability").assertExists()
        composeTestRule.onNodeWithText("Maximum HP").assertExists()
        composeTestRule.onNodeWithText("20 → 28").assertExists()
        composeTestRule.onNodeWithText("New level HP").assertExists()
        composeTestRule.onNodeWithText("+8").assertExists()
        composeTestRule.onNodeWithText("HP method").assertExists()
        composeTestRule.onNodeWithText("Fixed (6 on d10) + CON +2").assertExists()
        composeTestRule.onNodeWithText("d10 hit dice").assertExists()
        assertThat(composeTestRule.onAllNodesWithText("2 → 3").fetchSemanticsNodes()).hasSize(3)
    }

    @Test
    fun `CharacterLevelUpScreen should show rolled HP and all hit-die pools when reviewing multiclass level-up`() {
        // Given
        val fighter = characterClass("fighter", "Fighter", hitDie = 10)
        val wizard = characterClass("wizard", "Wizard", hitDie = 6)
        val state = reviewState(
            selectedClassId = wizard.id,
            classes = listOf(fighter, wizard),
            hitPointGain = HitPointGain.Rolled(5),
            hitDie = 6,
            before = snapshot(
                totalLevel = 4,
                classLevels = mapOf(fighter.id to 3, wizard.id to 1),
                classDisplayName = "Fighter 3 / Wizard 1",
                maximumHitPoints = 35,
                hitDicePools = listOf(LevelUpHitDicePool(6, 1), LevelUpHitDicePool(10, 3)),
                constitution = 14,
            ),
            after = snapshot(
                totalLevel = 5,
                classLevels = mapOf(fighter.id to 3, wizard.id to 2),
                classDisplayName = "Fighter 3 / Wizard 2",
                maximumHitPoints = 42,
                hitDicePools = listOf(LevelUpHitDicePool(6, 2), LevelUpHitDicePool(10, 3)),
                constitution = 14,
            ),
        )

        // When
        setContent(state)

        // Then
        composeTestRule.onNodeWithText("+7").assertExists()
        composeTestRule.onNodeWithText("Rolled (5 on d6) + CON +2").assertExists()
        composeTestRule.onNodeWithText("d6 hit dice").assertExists()
        composeTestRule.onNodeWithText("d10 hit dice").assertExists()
        assertThat(composeTestRule.onAllNodesWithText("1 → 2").fetchSemanticsNodes()).hasSize(2)
        composeTestRule.onNodeWithText("3 → 3").assertExists()
    }

    @Test
    fun `CharacterLevelUpScreen should label manual HP method when reviewing level-up`() {
        // Given
        val rogue = characterClass("rogue", "Rogue", hitDie = 8)
        val state = reviewState(
            selectedClassId = rogue.id,
            classes = listOf(rogue),
            hitPointGain = HitPointGain.Manual(9),
            hitDie = 8,
            before = snapshot(
                totalLevel = 3,
                classLevels = mapOf(rogue.id to 3),
                classDisplayName = "Rogue 3",
                maximumHitPoints = 30,
                hitDicePools = listOf(LevelUpHitDicePool(8, 3)),
                constitution = 12,
            ),
            after = snapshot(
                totalLevel = 4,
                classLevels = mapOf(rogue.id to 4),
                classDisplayName = "Rogue 4",
                maximumHitPoints = 40,
                hitDicePools = listOf(LevelUpHitDicePool(8, 4)),
                constitution = 12,
            ),
        )

        // When
        setContent(state)

        // Then
        composeTestRule.onNodeWithText("+10").assertExists()
        composeTestRule.onNodeWithText("Manual (9) + CON +1").assertExists()
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
        hitPointGain: HitPointGain,
        hitDie: Int,
        before: LevelUpSnapshot,
        after: LevelUpSnapshot,
    ) = CharacterLevelUpUiState.Content(
        characterName = "Test hero",
        plan = LevelUpPlan(
            expectedTotalLevel = before.totalLevel,
            rulesetId = "srd-5e-2014-v1",
            referenceDataVersion = "test-v1",
            selectedClassId = selectedClassId,
            selections = LevelUpSelections(hitPointGain = hitPointGain),
        ),
        preview = LevelUpPreview(
            before = before,
            after = after,
            requirements = listOf(LevelUpRequirement.HitPoints(
                hitDie = hitDie,
                fixedGain = hitDie / 2 + 1,
                selectedGain = hitPointGain,
            )),
            validations = emptyList(),
        ),
        classes = classes,
        feats = emptyList(),
        spells = emptyList(),
        steps = listOf(CharacterLevelUpStep.HitPoints, CharacterLevelUpStep.Review),
        step = CharacterLevelUpStep.Review,
        currentStepIndex = 1,
    )

    private fun snapshot(
        totalLevel: Int,
        classLevels: Map<String, Int>,
        classDisplayName: String,
        maximumHitPoints: Int,
        hitDicePools: List<LevelUpHitDicePool>,
        constitution: Int,
    ) = LevelUpSnapshot(
        totalLevel = totalLevel,
        classLevels = classLevels,
        classDisplayName = classDisplayName,
        proficiencyBonus = 2,
        abilityScores = AbilityScores(constitution = constitution),
        maximumHitPoints = maximumHitPoints,
        hitDicePools = hitDicePools,
        proficiencyIds = emptySet(),
        savingThrowAbilityIds = emptySet(),
        featureIds = emptySet(),
        sharedCasterLevel = 0,
        sharedSpellSlots = emptyMap(),
    )

    private fun characterClass(id: String, name: String, hitDie: Int) = CharacterClass(
        id = id,
        name = name,
        hitDie = hitDie,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )
}
