package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityScores
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
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterLevelUpGuidanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState should propagate informational findings from rebuilt preview`() =
        runTest(mainDispatcherRule.dispatcher) {
            val loadCharacter = mockk<LoadCharacterWithProgressionUseCase>()
            val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
            val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
            val observeFeats = mockk<ObserveAllFeatsUseCase>()
            val observeSpells = mockk<ObserveAllSpellsUseCase>()
            val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
            val createPlan = mockk<CreateLevelUpPlanUseCase>()
            val rebuildPlan = mockk<RebuildLevelUpPlanUseCase>()
            val applyLevelUp = mockk<ApplyLevelUpUseCase>()
            val progression = progression()
            val plan = LevelUpPlan(
                expectedTotalLevel = progression.totalLevel,
                rulesetId = progression.rulesetId,
                referenceDataVersion = progression.referenceDataVersion,
                selectedClassId = "fighter",
            )
            val guidance = LevelUpValidationIssue(
                code = LevelUpValidationCode.ExperienceThreshold,
                message = "Guidance from the progression engine.",
                severity = LevelUpValidationSeverity.Informational,
            )
            val classSelection = LevelUpRequirement.ClassSelection(
                eligibleClassIds = listOf("fighter"),
                selectedClassId = "fighter",
            )

            every { loadCharacter(CHARACTER_ID) } returns flowOf(
                CharacterWithProgression(
                    sheet = CharacterSheet(id = CHARACTER_ID, name = "Mira", level = progression.totalLevel),
                    progressionState = ProgressionState.Managed(progression),
                ),
            )
            every { observeClasses() } returns flowOf(Loadable.Content(listOf(characterClass())))
            every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
            every { observeFeats() } returns flowOf(Loadable.Content(emptyList()))
            every { observeSpells() } returns flowOf(Loadable.Content(emptyList()))
            every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
            every { createPlan(any()) } returns plan
            every { rebuildPlan(any(), any(), any(), any()) } returns preview(
                plan = plan,
                requirements = listOf(classSelection),
                validations = listOf(guidance),
            )

            val viewModel = CharacterLevelUpViewModel(
                loadCharacter = loadCharacter,
                observeClasses = observeClasses,
                observeFeatures = observeFeatures,
                observeFeats = observeFeats,
                observeSpells = observeSpells,
                observeLanguages = observeLanguages,
                createPlan = createPlan,
                rebuildPlan = rebuildPlan,
                applyLevelUp = applyLevelUp,
                savedStateHandle = SavedStateHandle(mapOf("characterId" to CHARACTER_ID)),
            )
            val collector = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            val content = viewModel.uiState.value as CharacterLevelUpUiState.Content
            assertThat(content.preview.validations).containsExactly(guidance)
            assertThat(content.informationalIssues).containsExactly(guidance)
            assertThat(content.blockingIssues).isEmpty()
            assertThat(content.overrideableIssues).isEmpty()

            collector.cancel()
        }

    private fun progression() = CharacterProgression(
        referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
        origin = ProgressionOrigin.Guided,
        levels = listOf(
            CharacterLevelRecord(
                characterLevel = 1,
                classId = "fighter",
                classLevel = 1,
                hitPointGain = HitPointGain.Fixed(10),
            ),
        ),
    )

    private fun characterClass() = CharacterClass(
        id = "fighter",
        name = "Fighter",
        hitDie = 10,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )

    private fun preview(
        plan: LevelUpPlan,
        requirements: List<LevelUpRequirement>,
        validations: List<LevelUpValidationIssue>,
    ): LevelUpPreview {
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
            validations = validations,
        )
    }

    private companion object {
        const val CHARACTER_ID = "character-1"
    }
}