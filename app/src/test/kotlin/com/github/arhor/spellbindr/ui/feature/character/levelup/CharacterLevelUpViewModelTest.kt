package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.usecase.ApplyLevelUpUseCase
import com.github.arhor.spellbindr.domain.usecase.CreateLevelUpPlanUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterWithProgressionUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeatsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.RebuildLevelUpPlanUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterLevelUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState should restore every wizard selection when ViewModel is recreated`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
            val requirements: (LevelUpPlan) -> List<LevelUpRequirement> = ::allSelectionRequirements
            val first = createViewModel(savedStateHandle = savedStateHandle, requirements = requirements)
            val firstCollector = launch { first.vm.uiState.collect { } }
            advanceUntilIdle()
            val spellChanges = SpellChanges(
                learned = setOf(ClassSpellRef("fighter", "light")),
                addedToSpellbook = setOf(ClassSpellRef("fighter", "shield")),
                featureLearned = mapOf("feature-spell" to setOf(ClassSpellRef("fighter", "guidance"))),
                replacementSourceSpellId = "old-spell",
            )

            first.vm.dispatch(CharacterLevelUpIntent.ClassSelected("fighter"))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.SubclassSelected("champion"))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.ChoiceToggled("feature-choice", "feature-a", 1))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.ChoiceToggled("proficiency-choice", "skill-arcana", 1))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(9)))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
                AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1)),
            ))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.ChoiceToggled("feat-choice", AbilityIds.CON, 1))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.SpellChangesSelected(spellChanges))
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.AcknowledgementChanged("MulticlassPrerequisite", true))
            advanceUntilIdle()
            firstCollector.cancel()

            // When
            val restored = createViewModel(savedStateHandle = savedStateHandle, requirements = requirements)
            val restoredCollector = launch { restored.vm.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val plan = (restored.vm.uiState.value as CharacterLevelUpUiState.Content).plan
            assertThat(plan.selectedClassId).isEqualTo("fighter")
            assertThat(plan.selections.subclassId).isEqualTo("champion")
            assertThat(plan.selections.featureChoices).containsEntry("feature-choice", setOf("feature-a"))
            assertThat(plan.selections.proficiencyChoices)
                .containsEntry("proficiency-choice", setOf("skill-arcana"))
            assertThat(plan.selections.hitPointGain).isEqualTo(HitPointGain.Manual(9))
            assertThat(plan.selections.abilityScoreDecision).isEqualTo(
                AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1)),
            )
            assertThat(plan.selections.featChoices).containsEntry("feat-choice", setOf(AbilityIds.CON))
            assertThat(plan.selections.spellChanges).isEqualTo(spellChanges)
            assertThat(plan.selections.acknowledgedIssueCodes).contains("MulticlassPrerequisite")
            restoredCollector.cancel()
        }

    @Test
    fun `uiState should invalidate restored draft when character level changes`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
            val first = createViewModel(savedStateHandle = savedStateHandle, progression = progression(1))
            val firstCollector = launch { first.vm.uiState.collect { } }
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(9)))
            advanceUntilIdle()
            firstCollector.cancel()

            // When
            val restored = createViewModel(savedStateHandle = savedStateHandle, progression = progression(2))
            val restoredCollector = launch { restored.vm.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val content = restored.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.plan.expectedTotalLevel).isEqualTo(2)
            assertThat(content.plan.selections.hitPointGain).isNull()
            assertThat(savedStateHandle.get<String>(DRAFT_KEY)).isNull()
            restoredCollector.cancel()
        }

    @Test
    fun `uiState should invalidate restored draft when ruleset changes`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
            val first = createViewModel(savedStateHandle = savedStateHandle)
            val firstCollector = launch { first.vm.uiState.collect { } }
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(9)))
            advanceUntilIdle()
            firstCollector.cancel()

            // When
            val restored = createViewModel(
                savedStateHandle = savedStateHandle,
                progression = progression(1, rulesetId = "other-rules"),
            )
            val restoredCollector = launch { restored.vm.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val content = restored.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.plan.rulesetId).isEqualTo("other-rules")
            assertThat(content.plan.selections.hitPointGain).isNull()
            assertThat(savedStateHandle.get<String>(DRAFT_KEY)).isNull()
            restoredCollector.cancel()
        }

    @Test
    fun `uiState should invalidate restored draft when reference version changes`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
            val first = createViewModel(savedStateHandle = savedStateHandle)
            val firstCollector = launch { first.vm.uiState.collect { } }
            advanceUntilIdle()
            first.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(9)))
            advanceUntilIdle()
            firstCollector.cancel()
            val draftJson = checkNotNull(savedStateHandle.get<String>(DRAFT_KEY))
            savedStateHandle[DRAFT_KEY] = draftJson.replace(
                LevelUpReferenceRules.referenceDataVersion,
                "outdated-reference-data",
            )

            // When
            val restored = createViewModel(savedStateHandle = savedStateHandle)
            val restoredCollector = launch { restored.vm.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val content = restored.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.plan.referenceDataVersion).isEqualTo(LevelUpReferenceRules.referenceDataVersion)
            assertThat(content.plan.selections.hitPointGain).isNull()
            assertThat(savedStateHandle.get<String>(DRAFT_KEY)).isNull()
            restoredCollector.cancel()
        }

    @Test
    fun `dispatch should emit cancelled without applying level when cancel is clicked`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fixture = createViewModel()
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            val effect = async { fixture.vm.effects.first() }
            advanceUntilIdle()

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.CancelClicked)
            advanceUntilIdle()

            // Then
            assertThat(effect.await()).isEqualTo(CharacterLevelUpEffect.Cancelled)
            coVerify(exactly = 0) { fixture.applyLevelUp(any(), any(), any(), any()) }
            stateCollector.cancel()
        }

    @Test
    fun `dispatch should retain reviewed selections and complete when persistence retry succeeds`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fixture = createViewModel(applyResult = ApplyLevelUpResult.PersistenceFailure("disk full"))
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(11)))
            advanceUntilIdle()
            moveToReview(fixture.vm)

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()
            val failedContent = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            coEvery { fixture.applyLevelUp(any(), any(), any(), any()) } returns
                ApplyLevelUpResult.Success(CharacterSheet(id = CHARACTER_ID), progression(2))
            val completion = async { fixture.vm.effects.first() }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()

            // Then
            assertThat(failedContent.step).isEqualTo(CharacterLevelUpStep.Review)
            assertThat(failedContent.isSaving).isFalse()
            assertThat(failedContent.persistenceMessage).isEqualTo("disk full")
            assertThat(failedContent.plan.selections.hitPointGain).isEqualTo(HitPointGain.Manual(11))
            assertThat(completion.await()).isEqualTo(CharacterLevelUpEffect.Completed)
            coVerify(exactly = 2) { fixture.applyLevelUp(any(), any(), any(), any()) }
            stateCollector.cancel()
        }

    @Test
    fun `dispatch should reload fresh draft and permit confirmation when confirmation is stale`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fixture = createViewModel(applyResult = ApplyLevelUpResult.StaleState)
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(11)))
            advanceUntilIdle()
            moveToReview(fixture.vm)

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()
            val staleContent = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            fixture.vm.dispatch(CharacterLevelUpIntent.ReloadClicked)
            advanceUntilIdle()
            val reloadedContent = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            moveToReview(fixture.vm)
            coEvery { fixture.applyLevelUp(any(), any(), any(), any()) } returns
                ApplyLevelUpResult.Success(CharacterSheet(id = CHARACTER_ID), progression(2))
            val completion = async { fixture.vm.effects.first() }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()

            // Then
            assertThat(staleContent.step).isEqualTo(CharacterLevelUpStep.Review)
            assertThat(staleContent.isSaving).isFalse()
            assertThat(staleContent.staleMessage).contains("character changed")
            assertThat(staleContent.plan.selections.hitPointGain).isEqualTo(HitPointGain.Manual(11))
            assertThat(reloadedContent.step).isEqualTo(CharacterLevelUpStep.Class)
            assertThat(reloadedContent.plan.selections.hitPointGain).isNull()
            assertThat(completion.await()).isEqualTo(CharacterLevelUpEffect.Completed)
            coVerify(exactly = 2) { fixture.applyLevelUp(any(), any(), any(), any()) }
            stateCollector.cancel()
        }

    @Test
    fun `dispatch should preserve saving draft when acknowledgement changes during confirmation`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val applyResult = CompletableDeferred<ApplyLevelUpResult>()
            val fixture = createViewModel(deferredApplyResult = applyResult)
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            moveToReview(fixture.vm)
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()
            val savingPlan = (fixture.vm.uiState.value as CharacterLevelUpUiState.Content).plan

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.AcknowledgementChanged("MulticlassPrerequisite", true))
            advanceUntilIdle()

            // Then
            val content = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.isSaving).isTrue()
            assertThat(content.plan).isEqualTo(savingPlan)
            assertThat(content.plan.selections.acknowledgedIssueCodes).isEmpty()
            coVerify(exactly = 1) { fixture.applyLevelUp(any(), any(), any(), any()) }
            stateCollector.cancel()
            applyResult.complete(ApplyLevelUpResult.StaleState)
        }

    @Test
    fun `dispatch should apply exactly once and emit one completion when confirm is clicked repeatedly`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val applyResult = CompletableDeferred<ApplyLevelUpResult>()
            val fixture = createViewModel(deferredApplyResult = applyResult)
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            val effect = async { fixture.vm.effects.first() }
            advanceUntilIdle()
            moveToReview(fixture.vm)

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            fixture.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()
            applyResult.complete(ApplyLevelUpResult.Success(CharacterSheet(id = CHARACTER_ID), progression(2)))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { fixture.applyLevelUp(any(), any(), any(), any()) }
            assertThat(effect.await()).isEqualTo(CharacterLevelUpEffect.Completed)
            assertThat(fixture.savedStateHandle.get<String>(DRAFT_KEY)).isNull()
            stateCollector.cancel()
        }

    @Test
    fun `uiState should clear interrupted saving state when ViewModel is recreated`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val applyResult = CompletableDeferred<ApplyLevelUpResult>()
            val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
            val first = createViewModel(savedStateHandle = savedStateHandle, deferredApplyResult = applyResult)
            val firstCollector = launch { first.vm.uiState.collect { } }
            advanceUntilIdle()
            moveToReview(first.vm)
            first.vm.dispatch(CharacterLevelUpIntent.ConfirmClicked)
            advanceUntilIdle()
            assertThat((first.vm.uiState.value as CharacterLevelUpUiState.Content).isSaving).isTrue()
            firstCollector.cancel()

            // When
            val restored = createViewModel(savedStateHandle = savedStateHandle)
            val restoredCollector = launch { restored.vm.uiState.collect { } }
            advanceUntilIdle()

            // Then
            val content = restored.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.step).isEqualTo(CharacterLevelUpStep.Review)
            assertThat(content.isSaving).isFalse()
            assertThat(content.canConfirm).isTrue()
            restoredCollector.cancel()
            applyResult.complete(ApplyLevelUpResult.StaleState)
        }

    @Test
    fun `uiState should return to first available step when class selection removes current step`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val requirements: (LevelUpPlan) -> List<LevelUpRequirement> = { plan ->
                listOf(LevelUpRequirement.ClassSelection(
                    eligibleClassIds = listOf("fighter", "rogue"),
                    selectedClassId = plan.selectedClassId,
                )) + if (plan.selectedClassId == "fighter") {
                    listOf(LevelUpRequirement.SpellDecisions(
                        id = "fighter:2:spells",
                        classId = "fighter",
                        classLevel = 2,
                        policyId = "known",
                        changes = plan.selections.spellChanges,
                    ))
                } else {
                    emptyList()
                }
            }
            val fixture = createViewModel(requirements = requirements)
            val stateCollector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.NextClicked)
            advanceUntilIdle()
            assertThat((fixture.vm.uiState.value as CharacterLevelUpUiState.Content).step)
                .isEqualTo(CharacterLevelUpStep.Spells)

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.ClassSelected("rogue"))
            advanceUntilIdle()

            // Then
            val content = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.steps).containsExactly(CharacterLevelUpStep.Class, CharacterLevelUpStep.Review).inOrder()
            assertThat(content.step).isEqualTo(CharacterLevelUpStep.Class)
            stateCollector.cancel()
        }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID)),
        progression: CharacterProgression = progression(1),
        requirements: (LevelUpPlan) -> List<LevelUpRequirement> = ::defaultRequirements,
        applyResult: ApplyLevelUpResult = ApplyLevelUpResult.StaleState,
        deferredApplyResult: CompletableDeferred<ApplyLevelUpResult>? = null,
    ): Fixture {
        val loadCharacter = mockk<LoadCharacterWithProgressionUseCase>()
        val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
        val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
        val observeFeats = mockk<ObserveAllFeatsUseCase>()
        val observeSpells = mockk<ObserveAllSpellsUseCase>()
        val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
        val createPlan = mockk<CreateLevelUpPlanUseCase>()
        val rebuildPlan = mockk<RebuildLevelUpPlanUseCase>()
        val applyLevelUp = mockk<ApplyLevelUpUseCase>()
        every { loadCharacter(CHARACTER_ID) } returns flowOf(
            CharacterWithProgression(CharacterSheet(id = CHARACTER_ID, name = "Mira"), ProgressionState.Managed(progression)),
        )
        every { observeClasses() } returns flowOf(Loadable.Content(listOf(characterClass("fighter"), characterClass("rogue"))))
        every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
        every { observeFeats() } returns flowOf(Loadable.Content(emptyList()))
        every { observeSpells() } returns flowOf(Loadable.Content(emptyList()))
        every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
        every { createPlan(any()) } answers {
            val source = firstArg<CharacterProgression>()
            LevelUpPlan(
                expectedTotalLevel = source.totalLevel,
                rulesetId = source.rulesetId,
                referenceDataVersion = source.referenceDataVersion,
                selectedClassId = "fighter",
            )
        }
        every { rebuildPlan(any(), any(), any(), any()) } answers {
            val plan = thirdArg<LevelUpPlan>()
            preview(plan, requirements(plan))
        }
        if (deferredApplyResult != null) {
            coEvery { applyLevelUp(any(), any(), any(), any()) } coAnswers { deferredApplyResult.await() }
        } else {
            coEvery { applyLevelUp(any(), any(), any(), any()) } returns applyResult
        }
        return Fixture(
            vm = CharacterLevelUpViewModel(
                loadCharacter,
                observeClasses,
                observeFeatures,
                observeFeats,
                observeSpells,
                observeLanguages,
                createPlan,
                rebuildPlan,
                applyLevelUp,
                savedStateHandle,
            ),
            applyLevelUp = applyLevelUp,
            savedStateHandle = savedStateHandle,
        )
    }

    private fun moveToReview(vm: CharacterLevelUpViewModel) {
        vm.dispatch(CharacterLevelUpIntent.NextClicked)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertThat((vm.uiState.value as CharacterLevelUpUiState.Content).step).isEqualTo(CharacterLevelUpStep.Review)
    }

    private data class Fixture(
        val vm: CharacterLevelUpViewModel,
        val applyLevelUp: ApplyLevelUpUseCase,
        val savedStateHandle: SavedStateHandle,
    )

    companion object {
        private const val CHARACTER_ID = "character-1"
        private const val CHARACTER_ID_KEY = "characterId"
        private const val DRAFT_KEY = "character-level-up-draft"

        private fun progression(
            level: Int,
            rulesetId: String = CharacterProgression.SUPPORTED_RULESET_ID,
        ) = CharacterProgression(
            rulesetId = rulesetId,
            referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
            origin = ProgressionOrigin.Guided,
            levels = (1..level).map { characterLevel ->
                CharacterLevelRecord(
                    characterLevel = characterLevel,
                    classId = "fighter",
                    classLevel = characterLevel,
                    hitPointGain = HitPointGain.Fixed(6),
                )
            },
        )

        private fun characterClass(id: String) = CharacterClass(
            id = id,
            name = id.replaceFirstChar(Char::titlecase),
            hitDie = 10,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            subclasses = emptyList(),
            levels = emptyList(),
        )

        private fun defaultRequirements(plan: LevelUpPlan): List<LevelUpRequirement> = listOf(
            LevelUpRequirement.ClassSelection(
                eligibleClassIds = listOf("fighter", "rogue"),
                selectedClassId = plan.selectedClassId,
            ),
        )

        private fun allSelectionRequirements(plan: LevelUpPlan): List<LevelUpRequirement> = listOf(
            LevelUpRequirement.ClassSelection(
                eligibleClassIds = listOf("fighter", "rogue"),
                selectedClassId = plan.selectedClassId,
            ),
            LevelUpRequirement.SubclassSelection(
                id = "fighter:3:subclass",
                classId = "fighter",
                options = emptyList(),
                selectedSubclassId = plan.selections.subclassId,
            ),
            choiceRequirement("feature-choice", LevelUpChoiceCategory.Feature, plan.selections.featureChoices),
            choiceRequirement(
                "proficiency-choice",
                LevelUpChoiceCategory.Proficiency,
                plan.selections.proficiencyChoices,
            ),
            LevelUpRequirement.HitPoints(hitDie = 10, selectedGain = plan.selections.hitPointGain),
            LevelUpRequirement.AbilityScoreImprovement(
                id = "fighter:4:asi",
                classId = "fighter",
                abilityPoints = 2,
                maximumAbilityScore = 20,
                allowsFeat = true,
                selectedDecision = plan.selections.abilityScoreDecision,
            ),
            choiceRequirement("feat-choice", LevelUpChoiceCategory.Feat, plan.selections.featChoices),
            LevelUpRequirement.SpellDecisions(
                id = "fighter:2:spells",
                classId = "fighter",
                classLevel = 2,
                policyId = "known",
                changes = plan.selections.spellChanges,
            ),
        )

        private fun choiceRequirement(
            id: String,
            category: LevelUpChoiceCategory,
            selections: Map<String, Set<String>>,
        ) = LevelUpRequirement.ChoiceSelection(
            id = id,
            sourceId = "source",
            label = id,
            choice = Choice.OptionsArrayChoice(choose = 1, from = listOf("feature-a", "skill-arcana", AbilityIds.CON)),
            selectedOptionIds = selections[id].orEmpty(),
            category = category,
        )

        private fun preview(plan: LevelUpPlan, requirements: List<LevelUpRequirement>): LevelUpPreview {
            val before = LevelUpSnapshot(
                totalLevel = plan.expectedTotalLevel,
                classLevels = mapOf("fighter" to plan.expectedTotalLevel),
                classDisplayName = "Fighter ${plan.expectedTotalLevel}",
                proficiencyBonus = 2,
                abilityScores = AbilityScores(),
                maximumHitPoints = 10,
                hitDicePools = emptyList(),
                proficiencyIds = emptySet(),
                savingThrowAbilityIds = emptySet(),
                featureIds = emptySet(),
                sharedCasterLevel = 0,
                sharedSpellSlots = emptyMap(),
            )
            return LevelUpPreview(
                before = before,
                after = before.copy(totalLevel = before.totalLevel + 1),
                requirements = requirements,
                validations = emptyList(),
            )
        }
    }
}
