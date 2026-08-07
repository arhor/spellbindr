package com.github.arhor.spellbindr.ui.feature.character.levelup

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterLevelUpValidationStateTest {

    @Test
    fun `informational spell policy finding should not disable next`() {
        val requirement = LevelUpRequirement.SpellDecisions(
            id = "bard:2:spells",
            classId = "bard",
            classLevel = 2,
            policyId = "known",
            changes = SpellChanges(),
        )
        val guidance = validation(
            code = LevelUpValidationCode.SpellPolicy,
            severity = LevelUpValidationSeverity.Informational,
        )

        val state = content(
            step = CharacterLevelUpStep.Spells,
            requirements = listOf(requirement),
            validations = listOf(guidance),
        )

        assertThat(state.canAdvance).isTrue()
        assertThat(state.informationalIssues).containsExactly(guidance)
    }

    @Test
    fun `blocking spell policy finding should continue to disable next`() {
        val requirement = LevelUpRequirement.SpellDecisions(
            id = "bard:2:spells",
            classId = "bard",
            classLevel = 2,
            policyId = "known",
            changes = SpellChanges(),
        )
        val blocking = validation(
            code = LevelUpValidationCode.SpellPolicy,
            severity = LevelUpValidationSeverity.Blocking,
        )

        val state = content(
            step = CharacterLevelUpStep.Spells,
            requirements = listOf(requirement),
            validations = listOf(blocking),
        )

        assertThat(state.canAdvance).isFalse()
        assertThat(state.blockingIssues).containsExactly(blocking)
    }

    @Test
    fun `informational ability finding should not disable a complete ability decision`() {
        val decision = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 2))
        val requirement = LevelUpRequirement.AbilityScoreImprovement(
            id = "fighter:4:asi",
            classId = "fighter",
            abilityPoints = 2,
            maximumAbilityScore = 20,
            allowsFeat = true,
            selectedDecision = decision,
        )
        val guidance = validation(
            code = LevelUpValidationCode.AbilityScoreDecisionRequired,
            severity = LevelUpValidationSeverity.Informational,
        )

        val state = content(
            step = CharacterLevelUpStep.AbilityScore,
            requirements = listOf(requirement),
            validations = listOf(guidance),
            selections = LevelUpSelections(abilityScoreDecision = decision),
        )

        assertThat(state.canAdvance).isTrue()
    }

    private fun validation(
        code: LevelUpValidationCode,
        severity: LevelUpValidationSeverity,
    ) = LevelUpValidationIssue(code, "${severity.name} finding", severity)

    private fun content(
        step: CharacterLevelUpStep,
        requirements: List<LevelUpRequirement>,
        validations: List<LevelUpValidationIssue>,
        selections: LevelUpSelections = LevelUpSelections(),
    ): CharacterLevelUpUiState.Content {
        val before = snapshot()
        return CharacterLevelUpUiState.Content(
            characterName = "Test hero",
            plan = LevelUpPlan(
                expectedTotalLevel = before.totalLevel,
                rulesetId = "srd-5e-2014-v1",
                referenceDataVersion = "test-v1",
                selectedClassId = "fighter",
                selections = selections,
            ),
            preview = LevelUpPreview(
                before = before,
                after = before.copy(totalLevel = before.totalLevel + 1),
                requirements = requirements,
                validations = validations,
            ),
            classes = emptyList(),
            feats = emptyList(),
            spells = emptyList(),
            steps = listOf(step, CharacterLevelUpStep.Review),
            step = step,
            currentStepIndex = 0,
        )
    }

    private fun snapshot() = LevelUpSnapshot(
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
}