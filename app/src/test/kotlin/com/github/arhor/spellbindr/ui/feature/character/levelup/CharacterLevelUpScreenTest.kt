package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceOption
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpFeatureSpellGrantRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSpellOption
import com.github.arhor.spellbindr.domain.model.LevelUpSpellReplacementRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CharacterLevelUpScreen should dispatch class selection when class option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val fighter = characterClass("fighter", "Fighter")
        setContent(
            contentState(
                step = CharacterLevelUpStep.Class,
                classes = listOf(fighter),
                requirements = listOf(LevelUpRequirement.ClassSelection(
                    eligibleClassIds = listOf(fighter.id),
                    selectedClassId = null,
                )),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Fighter").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ClassSelected("fighter"))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch subclass selection when subclass option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(
            contentState(
                step = CharacterLevelUpStep.Choices,
                requirements = listOf(LevelUpRequirement.SubclassSelection(
                    id = "fighter:3:subclass",
                    classId = "fighter",
                    options = listOf(LevelUpChoiceOption("champion", "Champion")),
                    selectedSubclassId = null,
                )),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Champion").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SubclassSelected("champion"))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch feature choice when choice option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(
            contentState(
                step = CharacterLevelUpStep.Choices,
                requirements = listOf(LevelUpRequirement.ChoiceSelection(
                    id = "fighting-style",
                    sourceId = "fighter",
                    label = "Fighting Style",
                    choice = Choice.OptionsArrayChoice(1, listOf("defense")),
                    selectedOptionIds = emptySet(),
                    category = LevelUpChoiceCategory.Feature,
                    options = listOf(LevelUpChoiceOption("defense", "Defense")),
                )),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Defense").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ChoiceToggled("fighting-style", "defense", 1))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch fixed hit points when fixed option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Fixed (6)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Fixed(6)))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch rolled hit points when legal roll is entered`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Rolled result (1-10)").performTextInput("7")

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Rolled(7)))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch manual hit points when positive value is entered`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Manual result (positive)").performTextInput("14")

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(14)))
    }

    @Test
    fun `CharacterLevelUpScreen should clear hit points when manual value is not positive`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Manual result (positive)").performTextInput("0")

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.HitPointsCleared)
        composeTestRule.onNodeWithText("Next").assertIsNotEnabled()
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch two point increase when eligible ability is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(abilityState(), intents::add)

        // When
        composeTestRule.onNodeWithText("+2 STR (10 → 12)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
            AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 2)),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch split increase when second ability is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val selected = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1))
        setContent(abilityState(selectedDecision = selected), intents::add)

        // When
        composeTestRule.onNodeWithText("+1 DEX").performScrollTo().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
            AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1)),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should hide capped two point increase when ability cannot accept allocation`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(abilityState(abilities = AbilityScores(strength = 19)), intents::add)

        // When
        val cappedOption = composeTestRule.onNodeWithText("+2 STR (19 → 21)")

        // Then
        cappedOption.assertDoesNotExist()
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch feat decision when eligible feat is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val feat = Feat("athlete", "Athlete", emptyList())
        setContent(abilityState(feats = listOf(feat), eligibleFeatIds = listOf(feat.id)), intents::add)

        // When
        composeTestRule.onNodeWithText("Feat: Athlete").performScrollTo().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
            AbilityScoreDecision.Feat(feat.id),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch feat owned choice when ability option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val feat = Feat(
            id = "athlete",
            name = "Athlete",
            desc = emptyList(),
            abilityBonusChoice = Choice.AbilityBonusChoice(1, listOf(mapOf(AbilityIds.DEX to 1))),
        )
        setContent(
            abilityState(
                feats = listOf(feat),
                eligibleFeatIds = listOf(feat.id),
                selectedDecision = AbilityScoreDecision.Feat(feat.id),
                featChoice = LevelUpRequirement.ChoiceSelection(
                    id = "athlete:ability-bonus",
                    sourceId = feat.id,
                    label = feat.name,
                    choice = feat.abilityBonusChoice!!,
                    selectedOptionIds = emptySet(),
                    category = LevelUpChoiceCategory.Feat,
                    options = listOf(LevelUpChoiceOption(AbilityIds.DEX, "Athlete bonus: +1 DEX")),
                ),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Athlete bonus: +1 DEX").performScrollTo().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ChoiceToggled(
            "athlete:ability-bonus",
            AbilityIds.DEX,
            1,
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch feat proficiency choice when proficiency option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val feat = Feat(
            id = "skilled",
            name = "Skilled",
            desc = emptyList(),
            proficiencyChoice = Choice.OptionsArrayChoice(1, listOf("skill-arcana")),
        )
        setContent(
            abilityState(
                feats = listOf(feat),
                eligibleFeatIds = listOf(feat.id),
                selectedDecision = AbilityScoreDecision.Feat(feat.id),
                featChoice = LevelUpRequirement.ChoiceSelection(
                    id = feat.proficiencyChoiceId!!,
                    sourceId = feat.id,
                    label = feat.name,
                    choice = feat.proficiencyChoice!!,
                    selectedOptionIds = emptySet(),
                    category = LevelUpChoiceCategory.Feat,
                ),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("skill-arcana").performScrollTo().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ChoiceToggled(
            feat.proficiencyChoiceId!!,
            "skill-arcana",
            1,
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch feat language choice when language option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val feat = Feat(
            id = "linguist",
            name = "Linguist",
            desc = emptyList(),
            languageChoice = Choice.ResourceListChoice(1, "languages"),
        )
        setContent(
            abilityState(
                feats = listOf(feat),
                eligibleFeatIds = listOf(feat.id),
                selectedDecision = AbilityScoreDecision.Feat(feat.id),
                featChoice = LevelUpRequirement.ChoiceSelection(
                    id = feat.languageChoiceId!!,
                    sourceId = feat.id,
                    label = feat.name,
                    choice = feat.languageChoice!!,
                    selectedOptionIds = emptySet(),
                    category = LevelUpChoiceCategory.Feat,
                    options = listOf(LevelUpChoiceOption("elvish", "Elvish")),
                ),
            ),
            intents::add,
        )

        // When
        composeTestRule.onNodeWithText("Elvish").performScrollTo().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ChoiceToggled(
            feat.languageChoiceId!!,
            "elvish",
            1,
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should disable next when split ability allocation is incomplete`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val incomplete = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1))
        setContent(abilityState(selectedDecision = incomplete), intents::add)

        // When
        val next = composeTestRule.onNodeWithText("Next")

        // Then
        next.assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch next when split ability allocation is complete`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val complete = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1))
        setContent(abilityState(selectedDecision = complete), intents::add)

        // When
        composeTestRule.onNodeWithText("Next").performScrollTo().assertIsEnabled().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.NextClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should disable next when current requirement is unresolved`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        val next = composeTestRule.onNodeWithText("Next")

        // Then
        next.assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch next when current requirement is resolved`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(HitPointGain.Rolled(7)), intents::add)

        // When
        composeTestRule.onNodeWithText("Next").assertIsEnabled().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.NextClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch learned cantrip when cantrip option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val requirement = spellRequirement(
            classId = "druid",
            policyId = "prepared",
            requiredCantripCount = 1,
            cantripCandidates = listOf(LevelUpSpellOption("fire-bolt", "Fire Bolt", 0)),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("Fire Bolt").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(learned = setOf(ClassSpellRef("druid", "fire-bolt"))),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch known spell when known spell option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val requirement = spellRequirement(
            requiredKnownSpellCount = 1,
            knownSpellCandidates = listOf(LevelUpSpellOption("healing-word", "Healing Word", 1)),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("Healing Word (level 1)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(learned = setOf(ClassSpellRef("bard", "healing-word"))),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch spellbook addition when spellbook option is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val requirement = spellRequirement(
            classId = "wizard",
            policyId = "spellbook",
            requiredSpellbookAdditionCount = 1,
            spellbookCandidates = listOf(LevelUpSpellOption("shield", "Shield", 1)),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("Shield (level 1)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(addedToSpellbook = setOf(ClassSpellRef("wizard", "shield"))),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch Magical Secrets grant when any class spell is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val option = LevelUpSpellOption("fire-bolt", "Fire Bolt", 0)
        val requirement = spellRequirement(
            featureSpellGrants = listOf(LevelUpFeatureSpellGrantRequirement(
                featureId = "magical-secrets-1",
                label = "Magical Secrets",
                requiredCount = 2,
                candidates = listOf(option),
                selectedSpellIds = emptySet(),
            )),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("Fire Bolt").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(featureLearned = mapOf(
                "magical-secrets-1" to setOf(ClassSpellRef("bard", "fire-bolt")),
            )),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch pending replacement source when known source is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val source = LevelUpSpellOption("known-spell", "Known Spell", 1)
        val target = LevelUpSpellOption("new-spell", "New Spell", 1)
        val requirement = spellRequirement(
            replacement = LevelUpSpellReplacementRequirement(listOf(source), listOf(target)),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("Known Spell (level 1)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(replacementSourceSpellId = "known-spell"),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch completed replacement when replacement target is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val source = LevelUpSpellOption("known-spell", "Known Spell", 1)
        val target = LevelUpSpellOption("new-spell", "New Spell", 1)
        val changes = SpellChanges(replacementSourceSpellId = source.spellId)
        val requirement = spellRequirement(
            changes = changes,
            replacement = LevelUpSpellReplacementRequirement(
                sourceCandidates = listOf(source),
                replacementCandidates = listOf(target),
                selectedSourceSpellId = source.spellId,
            ),
        )
        setContent(spellState(requirement), intents::add)

        // When
        composeTestRule.onNodeWithText("New Spell (level 1)").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.SpellChangesSelected(
            SpellChanges(replaced = setOf(SpellReplacement("bard", "known-spell", "new-spell"))),
        ))
    }

    @Test
    fun `CharacterLevelUpScreen should disable next when spell count is unresolved`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val requirement = spellRequirement(
            requiredKnownSpellCount = 1,
            knownSpellCandidates = listOf(LevelUpSpellOption("healing-word", "Healing Word", 1)),
        )
        setContent(spellState(requirement), intents::add)

        // When
        val next = composeTestRule.onNodeWithText("Next")

        // Then
        next.assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch acknowledgement when review checkbox is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val issue = LevelUpValidationIssue(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            message = "Multiclass prerequisite is not met.",
            severity = LevelUpValidationSeverity.Overrideable,
        )
        setContent(reviewState(validations = listOf(issue)), intents::add)

        // When
        composeTestRule.onAllNodes(isToggleable())[0].performClick()

        // Then
        assertThat(intents).contains(
            CharacterLevelUpIntent.AcknowledgementChanged(issue.acknowledgementId, true),
        )
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch reload when stale draft action is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(reviewState(staleMessage = "The character changed."), intents::add)

        // When
        composeTestRule.onNodeWithText("Reload draft").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ReloadClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch confirm when valid review is confirmed`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(reviewState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Confirm level up").assertIsEnabled().performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.ConfirmClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch back when back action is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(reviewState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Back").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.BackClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should dispatch cancel when cancel action is clicked`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        setContent(hitPointState(), intents::add)

        // When
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        assertThat(intents).contains(CharacterLevelUpIntent.CancelClicked)
    }

    @Test
    fun `CharacterLevelUpScreen should disable review controls when confirmation is saving`() {
        // Given
        val intents = mutableListOf<CharacterLevelUpIntent>()
        val issue = LevelUpValidationIssue(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            message = "Multiclass prerequisite is not met.",
            severity = LevelUpValidationSeverity.Overrideable,
        )
        setContent(
            reviewState(
                validations = listOf(issue),
                staleMessage = "The character changed.",
                isSaving = true,
            ),
            intents::add,
        )

        // When
        val cancel = composeTestRule.onNodeWithText("Cancel")
        val back = composeTestRule.onNodeWithText("Back")
        val confirm = composeTestRule.onNodeWithText("Saving…")
        val reload = composeTestRule.onNodeWithText("Reload draft")
        val acknowledgement = composeTestRule.onAllNodes(isToggleable())[0]

        // Then
        cancel.assertIsNotEnabled()
        back.assertIsNotEnabled()
        confirm.assertIsNotEnabled()
        reload.assertIsNotEnabled()
        acknowledgement.assertIsNotEnabled()
        assertThat(intents).isEmpty()
    }

    private fun setContent(
        state: CharacterLevelUpUiState.Content,
        dispatch: CharacterLevelUpDispatch,
    ) {
        composeTestRule.setContent {
            AppTheme {
                CharacterLevelUpScreen(state = state, dispatch = dispatch)
            }
        }
    }

    private fun hitPointState(selectedGain: HitPointGain? = null): CharacterLevelUpUiState.Content {
        val requirement = LevelUpRequirement.HitPoints(
            hitDie = 10,
            fixedGain = 6,
            selectedGain = selectedGain,
        )
        return contentState(
            step = CharacterLevelUpStep.HitPoints,
            selections = LevelUpSelections(hitPointGain = selectedGain),
            requirements = listOf(requirement),
        )
    }

    private fun abilityState(
        abilities: AbilityScores = AbilityScores(),
        feats: List<Feat> = emptyList(),
        eligibleFeatIds: List<String> = emptyList(),
        selectedDecision: AbilityScoreDecision? = null,
        featChoice: LevelUpRequirement.ChoiceSelection? = null,
    ): CharacterLevelUpUiState.Content {
        val requirement = LevelUpRequirement.AbilityScoreImprovement(
            id = "fighter:4:asi",
            classId = "fighter",
            abilityPoints = 2,
            maximumAbilityScore = 20,
            allowsFeat = true,
            eligibleFeatIds = eligibleFeatIds,
            selectedDecision = selectedDecision,
        )
        return contentState(
            step = CharacterLevelUpStep.AbilityScore,
            abilities = abilities,
            feats = feats,
            selections = LevelUpSelections(abilityScoreDecision = selectedDecision),
            requirements = listOfNotNull(requirement, featChoice),
        )
    }

    private fun spellState(requirement: LevelUpRequirement.SpellDecisions): CharacterLevelUpUiState.Content =
        contentState(
            step = CharacterLevelUpStep.Spells,
            selections = LevelUpSelections(spellChanges = requirement.changes),
            requirements = listOf(requirement),
        )

    private fun reviewState(
        validations: List<LevelUpValidationIssue> = emptyList(),
        staleMessage: String? = null,
        isSaving: Boolean = false,
    ): CharacterLevelUpUiState.Content {
        val before = snapshot(AbilityScores())
        return contentState(
            step = CharacterLevelUpStep.Review,
            requirements = emptyList(),
        ).copy(
            preview = LevelUpPreview(before, before.copy(totalLevel = 4), emptyList(), validations),
            steps = listOf(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review),
            currentStepIndex = 1,
            staleMessage = staleMessage,
            isSaving = isSaving,
        )
    }

    private fun spellRequirement(
        classId: String = "bard",
        policyId: String = "known",
        changes: SpellChanges = SpellChanges(),
        requiredCantripCount: Int = 0,
        cantripCandidates: List<LevelUpSpellOption> = emptyList(),
        requiredKnownSpellCount: Int = 0,
        knownSpellCandidates: List<LevelUpSpellOption> = emptyList(),
        featureSpellGrants: List<LevelUpFeatureSpellGrantRequirement> = emptyList(),
        replacement: LevelUpSpellReplacementRequirement? = null,
        requiredSpellbookAdditionCount: Int = 0,
        spellbookCandidates: List<LevelUpSpellOption> = emptyList(),
    ) = LevelUpRequirement.SpellDecisions(
        id = "$classId:2:spells",
        classId = classId,
        classLevel = 2,
        policyId = policyId,
        changes = changes,
        requiredCantripCount = requiredCantripCount,
        cantripCandidates = cantripCandidates,
        requiredKnownSpellCount = requiredKnownSpellCount,
        knownSpellCandidates = knownSpellCandidates,
        featureSpellGrants = featureSpellGrants,
        replacement = replacement,
        requiredSpellbookAdditionCount = requiredSpellbookAdditionCount,
        spellbookCandidates = spellbookCandidates,
    )

    private fun contentState(
        step: CharacterLevelUpStep,
        abilities: AbilityScores = AbilityScores(),
        classes: List<CharacterClass> = emptyList(),
        feats: List<Feat> = emptyList(),
        selections: LevelUpSelections = LevelUpSelections(),
        requirements: List<LevelUpRequirement>,
    ): CharacterLevelUpUiState.Content {
        val before = snapshot(abilities)
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
            classes = classes,
            feats = feats,
            spells = emptyList(),
            steps = listOf(step, CharacterLevelUpStep.Review),
            step = step,
            currentStepIndex = 0,
        )
    }

    private fun snapshot(abilities: AbilityScores) = LevelUpSnapshot(
        totalLevel = 3,
        classLevels = mapOf("fighter" to 3),
        classDisplayName = "Fighter 3",
        proficiencyBonus = 2,
        abilityScores = abilities,
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
}
