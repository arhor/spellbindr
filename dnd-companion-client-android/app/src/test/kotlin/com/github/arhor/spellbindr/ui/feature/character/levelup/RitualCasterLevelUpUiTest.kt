package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RitualCasterLevelUpUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `ability score step exposes legal ritual caster choices and dispatches selections`() {
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(state(CharacterLevelUpStep.AbilityScore), intents::add)

        composeTestRule.onNodeWithText("Wizard").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Find Familiar").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Identify").performScrollTo().performClick()

        assertThat(intents).containsAtLeast(
            CharacterLevelUpIntent.ChoiceToggled("ritual-caster:class-list", "wizard", 1),
            CharacterLevelUpIntent.ChoiceToggled("ritual-caster:starting-spells", "find-familiar", 2),
            CharacterLevelUpIntent.ChoiceToggled("ritual-caster:starting-spells", "identify", 2),
        )
        composeTestRule.onNodeWithText("Shield").assertDoesNotExist()
        composeTestRule.onNodeWithText("Arcane Lock").assertDoesNotExist()
    }

    private fun setContent(state: CharacterLevelUpUiState.Content, dispatch: CharacterLevelUpDispatch) {
        composeTestRule.setContent { AppTheme { CharacterLevelUpScreen(state, dispatch) } }
    }

    private fun state(step: CharacterLevelUpStep): CharacterLevelUpUiState.Content {
        val feat = Feat("ritual-caster", "Ritual Caster", emptyList())
        val selections = LevelUpSelections(
            abilityScoreDecision = AbilityScoreDecision.Feat(feat.id),
            featChoices = mapOf(
                "ritual-caster:class-list" to setOf("wizard"),
                "ritual-caster:starting-spells" to emptySet(),
            ),
        )
        val requirements = listOf(
            LevelUpRequirement.AbilityScoreImprovement(
                id = "fighter:4:asi", classId = "fighter", abilityPoints = 2, maximumAbilityScore = 20,
                allowsFeat = true, eligibleFeatIds = listOf(feat.id), selectedDecision = selections.abilityScoreDecision,
            ),
            choice("ritual-caster:class-list", "Ritual Caster spell list", 1,
                listOf(LevelUpChoiceOption("wizard", "Wizard")), setOf("wizard")),
            choice("ritual-caster:starting-spells", "Ritual Caster starting rituals", 2,
                listOf(LevelUpChoiceOption("find-familiar", "Find Familiar"), LevelUpChoiceOption("identify", "Identify")), emptySet()),
        )
        val snapshot = LevelUpSnapshot(
            totalLevel = 3, classLevels = mapOf("fighter" to 3), classDisplayName = "Fighter 3", proficiencyBonus = 2,
            abilityScores = AbilityScores(), maximumHitPoints = 24, hitDicePools = emptyList(), proficiencyIds = emptySet(),
            savingThrowAbilityIds = emptySet(), featureIds = emptySet(), sharedCasterLevel = 0, sharedSpellSlots = emptyMap(),
        )
        return CharacterLevelUpUiState.Content(
            characterName = "Test hero",
            plan = LevelUpPlan(3, "srd-5e-2014-v1", "test-v1", "fighter", selections),
            preview = LevelUpPreview(snapshot, snapshot.copy(totalLevel = 4), requirements, emptyList()),
            classes = emptyList(), feats = listOf(feat), spells = emptyList(),
            steps = listOf(CharacterLevelUpStep.AbilityScore, CharacterLevelUpStep.Review), step = step,
            currentStepIndex = if (step == CharacterLevelUpStep.Review) 1 else 0,
        )
    }

    private fun choice(id: String, label: String, choose: Int, options: List<LevelUpChoiceOption>, selected: Set<String>) =
        LevelUpRequirement.ChoiceSelection(id, "ritual-caster", label, Choice.OptionsArrayChoice(choose, options.map { it.id }), selected, LevelUpChoiceCategory.Feat, options)
}
