package io.github.arhor.dnd.companion.ui.feature.character.levelup

import androidx.lifecycle.SavedStateHandle
import io.github.arhor.dnd.companion.MainDispatcherRule
import io.github.arhor.dnd.companion.domain.AssetBootstrapper
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.CharacterLevelRecord
import io.github.arhor.dnd.companion.domain.model.CharacterProgression
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.CharacterWithProgression
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpPreview
import io.github.arhor.dnd.companion.domain.model.LevelUpReferenceRules
import io.github.arhor.dnd.companion.domain.model.LevelUpRequirement
import io.github.arhor.dnd.companion.domain.model.LevelUpSnapshot
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationCode
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationIssue
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationSeverity
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.ProgressionOrigin
import io.github.arhor.dnd.companion.domain.model.ProgressionState
import io.github.arhor.dnd.companion.domain.usecase.ApplyLevelUpUseCase
import io.github.arhor.dnd.companion.domain.usecase.CreateLevelUpPlanUseCase
import io.github.arhor.dnd.companion.domain.usecase.LoadCharacterWithProgressionUseCase
import io.github.arhor.dnd.companion.domain.usecase.ObserveAllCharacterClassesUseCase
import io.github.arhor.dnd.companion.domain.usecase.ObserveAllFeatsUseCase
import io.github.arhor.dnd.companion.domain.usecase.ObserveAllFeaturesUseCase
import io.github.arhor.dnd.companion.domain.usecase.ObserveAllLanguagesUseCase
import io.github.arhor.dnd.companion.domain.usecase.ObserveAllSpellsUseCase
import io.github.arhor.dnd.companion.domain.usecase.RebuildLevelUpPlanUseCase
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
            val assetBootstrapper = mockk<AssetBootstrapper>()
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
                assetBootstrapper = assetBootstrapper,
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