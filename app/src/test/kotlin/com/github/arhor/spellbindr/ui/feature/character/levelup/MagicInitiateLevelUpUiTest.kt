package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
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
class MagicInitiateLevelUpUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `ability score step should collect every Magic Initiate choice`() {
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(state(CharacterLevelUpStep.AbilityScore), intents::add)

        composeTestRule.onNodeWithText("Wizard").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Fire Bolt").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Mage Hand").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Magic Missile").performScrollTo().performClick()

        assertThat(intents).containsAtLeast(
            CharacterLevelUpIntent.ChoiceToggled("magic-initiate:class-list", "wizard", 1),
            CharacterLevelUpIntent.ChoiceToggled("magic-initiate:cantrips", "fire-bolt", 2),
            CharacterLevelUpIntent.ChoiceToggled("magic-initiate:cantrips", "mage-hand", 2),
            CharacterLevelUpIntent.ChoiceToggled("magic-initiate:first-level-spell", "magic-missile", 1),
        )
    }

    @Test
    fun `review should show selected Magic Initiate spell list and spells`() {
        setContent(state(CharacterLevelUpStep.Review)) { _ -> }

        composeTestRule.onNodeWithText("Magic Initiate").assertExists()
        composeTestRule.onNodeWithText("Spell list: Wizard").assertExists()
        composeTestRule.onNodeWithText("Cantrips: Fire Bolt, Mage Hand").assertExists()
        composeTestRule.onNodeWithText("1st-level spell: Magic Missile").assertExists()
    }

    private fun setContent(
        state: CharacterLevelUpUiState.Content,
        dispatch: CharacterLevelUpDispatch,
    ) {
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpScreen(state, dispatch)
            }
        }
    }

    private fun state(step: CharacterLevelUpStep): CharacterLevelUpUiState.Content {
        val feat = Feat("magic-initiate", "Magic Initiate", emptyList())
        val selections = LevelUpSelections(
            abilityScoreDecision = AbilityScoreDecision.Feat(feat.id),
            featChoices = mapOf(
                "magic-initiate:class-list" to setOf("wizard"),
                "magic-initiate:cantrips" to setOf("fire-bolt", "mage-hand"),
                "magic-initiate:first-level-spell" to setOf("magic-missile"),
            ),
        )
        val requirements = listOf(
            LevelUpRequirement.AbilityScoreImprovement(
                id = "fighter:4:asi",
                classId = "fighter",
                abilityPoints = 2,
                maximumAbilityScore = 20,
                allowsFeat = true,
                eligibleFeatIds = listOf(feat.id),
                selectedDecision = selections.abilityScoreDecision,
            ),
            choice(
                "magic-initiate:class-list",
                "Magic Initiate spell list",
                1,
                listOf(LevelUpChoiceOption("wizard", "Wizard")),
                setOf("wizard"),
            ),
            choice(
                "magic-initiate:cantrips",
                "Magic Initiate cantrips",
                2,
                listOf(
                    LevelUpChoiceOption("fire-bolt", "Fire Bolt"),
                    LevelUpChoiceOption("mage-hand", "Mage Hand"),
                ),
                setOf("fire-bolt", "mage-hand"),
            ),
            choice(
                "magic-initiate:first-level-spell",
                "Magic Initiate 1st-level spell",
                1,
                listOf(LevelUpChoiceOption("magic-missile", "Magic Missile")),
                setOf("magic-missile"),
            ),
        )
        val before = LevelUpSnapshot(
            totalLevel = 3,
            classLevels = mapOf("fighter" to 3),
            classDisplayName = "Fighter 3",
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
        return CharacterLevelUpUiState.Content(
            characterName = "Test hero",
            plan = LevelUpPlan(
                expectedTotalLevel = 3,
                rulesetId = "srd-5e-2014-v1",
                referenceDataVersion = "test-v1",
                selectedClassId = "fighter",
                selections = selections,
            ),
            preview = LevelUpPreview(before, before.copy(totalLevel = 4), requirements, emptyList()),
            classes = emptyList(),
            feats = listOf(feat),
            spells = emptyList(),
            steps = listOf(CharacterLevelUpStep.AbilityScore, CharacterLevelUpStep.Review),
            step = step,
            currentStepIndex = if (step == CharacterLevelUpStep.Review) 1 else 0,
        )
    }

    private fun choice(
        id: String,
        label: String,
        choose: Int,
        options: List<LevelUpChoiceOption>,
        selected: Set<String>,
    ) = LevelUpRequirement.ChoiceSelection(
        id = id,
        sourceId = "magic-initiate",
        label = label,
        choice = Choice.OptionsArrayChoice(choose, options.map { it.id }),
        selectedOptionIds = selected,
        category = LevelUpChoiceCategory.Feat,
        options = options,
    )
}
