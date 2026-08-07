package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterLevelUpRetryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `retry should restore the current step and preserve draft selections after reference data recovers`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fixture = createFixture(retrySucceeds = true)
            val collector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            fixture.vm.dispatch(CharacterLevelUpIntent.ClassSelected("rogue"))
            advanceUntilIdle()
            fixture.classes.value = Loadable.Failure(cause = RuntimeException("temporary"))
            advanceUntilIdle()
            val failure = fixture.vm.uiState.value as CharacterLevelUpUiState.Failure
            assertThat(failure.canRetry).isTrue()

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.RetryClicked)
            advanceUntilIdle()

            // Then
            val content = fixture.vm.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.step).isEqualTo(CharacterLevelUpStep.Class)
            assertThat(content.plan.selectedClassId).isEqualTo("rogue")
            assertThat(fixture.savedStateHandle.get<String>(DRAFT_KEY)).isNotNull()
            coVerify(exactly = 1) { fixture.assetBootstrapper.retryFailedLoads() }
            collector.cancel()
        }

    @Test
    fun `retry should remain available when reference data fails repeatedly`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fixture = createFixture(retrySucceeds = false)
            val collector = launch { fixture.vm.uiState.collect { } }
            advanceUntilIdle()
            fixture.classes.value = Loadable.Failure(cause = RuntimeException("first failure"))
            advanceUntilIdle()

            // When
            fixture.vm.dispatch(CharacterLevelUpIntent.RetryClicked)
            advanceUntilIdle()
            val firstRetryFailure = fixture.vm.uiState.value as CharacterLevelUpUiState.Failure
            fixture.vm.dispatch(CharacterLevelUpIntent.RetryClicked)
            advanceUntilIdle()

            // Then
            assertThat(firstRetryFailure.canRetry).isTrue()
            assertThat(fixture.vm.uiState.value).isInstanceOf(CharacterLevelUpUiState.Failure::class.java)
            assertThat((fixture.vm.uiState.value as CharacterLevelUpUiState.Failure).canRetry).isTrue()
            coVerify(exactly = 2) { fixture.assetBootstrapper.retryFailedLoads() }
            collector.cancel()
        }

    private fun createFixture(retrySucceeds: Boolean): Fixture {
        val classes = MutableStateFlow<Loadable<List<CharacterClass>>>(
            Loadable.Content(listOf(characterClass("fighter"), characterClass("rogue"))),
        )
        val savedStateHandle = SavedStateHandle(mapOf(CHARACTER_ID_KEY to CHARACTER_ID))
        val loadCharacter = mockk<LoadCharacterWithProgressionUseCase>()
        val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
        val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
        val observeFeats = mockk<ObserveAllFeatsUseCase>()
        val observeSpells = mockk<ObserveAllSpellsUseCase>()
        val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
        val assetBootstrapper = mockk<AssetBootstrapper>()
        val createPlan = mockk<CreateLevelUpPlanUseCase>()
        val rebuildPlan = mockk<RebuildLevelUpPlanUseCase>()
        val applyLevelUp = mockk<ApplyLevelUpUseCase>()
        val progression = progression()

        every { loadCharacter(CHARACTER_ID) } returns flowOf(
            CharacterWithProgression(
                CharacterSheet(id = CHARACTER_ID, name = "Mira"),
                ProgressionState.Managed(progression),
            ),
        )
        every { observeClasses() } returns classes
        every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
        every { observeFeats() } returns flowOf(Loadable.Content(emptyList()))
        every { observeSpells() } returns flowOf(Loadable.Content(emptyList()))
        every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
        every { createPlan(any()) } answers {
            LevelUpPlan(
                expectedTotalLevel = progression.totalLevel,
                rulesetId = progression.rulesetId,
                referenceDataVersion = progression.referenceDataVersion,
                selectedClassId = "fighter",
            )
        }
        every { rebuildPlan(any(), any(), any(), any()) } answers {
            preview(thirdArg())
        }
        coEvery { applyLevelUp(any(), any(), any(), any()) } returns ApplyLevelUpResult.StaleState
        coEvery { assetBootstrapper.retryFailedLoads() } coAnswers {
            classes.value = Loadable.Loading
            classes.value = if (retrySucceeds) {
                Loadable.Content(listOf(characterClass("fighter"), characterClass("rogue")))
            } else {
                Loadable.Failure(cause = RuntimeException("still unavailable"))
            }
        }

        return Fixture(
            vm = CharacterLevelUpViewModel(
                loadCharacter,
                observeClasses,
                observeFeatures,
                observeFeats,
                observeSpells,
                observeLanguages,
                assetBootstrapper,
                createPlan,
                rebuildPlan,
                applyLevelUp,
                savedStateHandle,
            ),
            classes = classes,
            assetBootstrapper = assetBootstrapper,
            savedStateHandle = savedStateHandle,
        )
    }

    private data class Fixture(
        val vm: CharacterLevelUpViewModel,
        val classes: MutableStateFlow<Loadable<List<CharacterClass>>>,
        val assetBootstrapper: AssetBootstrapper,
        val savedStateHandle: SavedStateHandle,
    )

    companion object {
        private const val CHARACTER_ID = "character-1"
        private const val CHARACTER_ID_KEY = "characterId"
        private const val DRAFT_KEY = "character-level-up-draft"

        private fun progression() = CharacterProgression(
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                CharacterLevelRecord(
                    characterLevel = 1,
                    classId = "fighter",
                    classLevel = 1,
                    hitPointGain = HitPointGain.Fixed(6),
                ),
            ),
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

        private fun preview(plan: LevelUpPlan): LevelUpPreview {
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
                requirements = listOf(
                    LevelUpRequirement.ClassSelection(
                        eligibleClassIds = listOf("fighter", "rogue"),
                        selectedClassId = plan.selectedClassId,
                    ),
                ),
                validations = emptyList(),
            )
        }
    }
}
