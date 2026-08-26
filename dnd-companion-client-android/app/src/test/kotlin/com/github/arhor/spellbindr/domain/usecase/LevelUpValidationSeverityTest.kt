package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpPreview
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpSnapshot
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpValidationSeverityTest {

    @Test
    fun `informational validation should not block confirmation or require acknowledgement`() {
        val guidance = validation(
            code = LevelUpValidationCode.ExperienceThreshold,
            severity = LevelUpValidationSeverity.Informational,
        )

        val preview = preview(validations = listOf(guidance))

        assertThat(preview.canConfirm).isTrue()
        assertThat(preview.requirements.filterIsInstance<LevelUpRequirement.Acknowledgement>()).isEmpty()
    }

    @Test
    fun `informational validation should coexist with blocking and overrideable findings`() {
        val guidance = validation(
            code = LevelUpValidationCode.ExperienceThreshold,
            severity = LevelUpValidationSeverity.Informational,
        )
        val overrideable = validation(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            severity = LevelUpValidationSeverity.Overrideable,
        )
        val blocking = validation(
            code = LevelUpValidationCode.ChoiceRequired,
            severity = LevelUpValidationSeverity.Blocking,
        )
        val acknowledgedWarning = LevelUpRequirement.Acknowledgement(
            id = overrideable.acknowledgementId,
            issue = overrideable,
            acknowledged = true,
        )

        assertThat(preview(listOf(guidance, overrideable), listOf(acknowledgedWarning)).canConfirm).isTrue()
        assertThat(preview(listOf(guidance, blocking, overrideable), listOf(acknowledgedWarning)).canConfirm).isFalse()
    }

    @Test
    fun `recordFor should persist acknowledgements for overrideable findings only`() {
        val guidance = validation(
            code = LevelUpValidationCode.ExperienceThreshold,
            severity = LevelUpValidationSeverity.Informational,
        )
        val overrideable = validation(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            severity = LevelUpValidationSeverity.Overrideable,
        )
        val clazz = characterClass()
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = emptyList(),
        )
        val plan = LevelUpPlan(
            expectedTotalLevel = 0,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = "test-v1",
            selectedClassId = clazz.id,
            selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(clazz.hitDie),
                acknowledgedIssueCodes = setOf(
                    guidance.acknowledgementId,
                    overrideable.acknowledgementId,
                ),
            ),
        )

        val record = LevelUpProgressionEngine.recordFor(
            plan = plan,
            clazz = clazz,
            classLevel = 1,
            progression = progression,
            referenceData = LevelUpReferenceData(
                classes = listOf(clazz),
                features = emptyList(),
                referenceDataVersion = "test-v1",
            ),
            validations = listOf(guidance, overrideable),
        )

        assertThat(record.ruleAcknowledgements).containsExactly(overrideable.acknowledgementId)
    }

    @Test
    fun `duplicate validation codes retain independent stable acknowledgements`() {
        val first = validation(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            severity = LevelUpValidationSeverity.Overrideable,
            findingId = "multiclass-prerequisite:fighter",
        )
        val second = validation(
            code = LevelUpValidationCode.MulticlassPrerequisite,
            severity = LevelUpValidationSeverity.Overrideable,
            findingId = "multiclass-prerequisite:wizard",
        )
        val requirements = listOf(first, second).map {
            LevelUpRequirement.Acknowledgement(it.acknowledgementId, it, acknowledged = true)
        }

        assertThat(first.acknowledgementId).isNotEqualTo(second.acknowledgementId)
        assertThat(preview(listOf(first, second), requirements).canConfirm).isTrue()
    }

    private fun validation(
        code: LevelUpValidationCode,
        severity: LevelUpValidationSeverity,
        findingId: String? = null,
    ) = LevelUpValidationIssue(code, "${severity.name} finding", severity, findingId)

    private fun preview(
        validations: List<LevelUpValidationIssue>,
        requirements: List<LevelUpRequirement> = emptyList(),
    ): LevelUpPreview {
        val snapshot = snapshot()
        return LevelUpPreview(
            before = snapshot,
            after = snapshot.copy(totalLevel = snapshot.totalLevel + 1),
            requirements = requirements,
            validations = validations,
        )
    }

    private fun snapshot() = LevelUpSnapshot(
        totalLevel = 1,
        classLevels = mapOf("fighter" to 1),
        classDisplayName = "Fighter 1",
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
}
