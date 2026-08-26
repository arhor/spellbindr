package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.AbilityIds
import io.github.arhor.dnd.companion.domain.model.AbilityScorePrerequisite
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.CharacterLevelRecord
import io.github.arhor.dnd.companion.domain.model.CharacterProgression
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.Choice
import io.github.arhor.dnd.companion.domain.model.ClassLevel
import io.github.arhor.dnd.companion.domain.model.Feature
import io.github.arhor.dnd.companion.domain.model.Feat
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpReferenceData
import io.github.arhor.dnd.companion.domain.model.LevelUpSelections
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationCode
import io.github.arhor.dnd.companion.domain.model.LevelUpValidationSeverity
import io.github.arhor.dnd.companion.domain.model.MultiClassing
import io.github.arhor.dnd.companion.domain.model.ProgressionOrigin
import io.github.arhor.dnd.companion.domain.model.Prerequisite
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineChoiceTest {

    @Test
    fun `rebuild should require acknowledgement when a new class multiclass prerequisite is not met`() {
        val progression = progression("fighter")
        val sheet = CharacterSheet(
            id = "character",
            level = 1,
            abilityScores = AbilityScores(strength = 13, intelligence = 12),
        )
        val plan = plan(1, "wizard", HitPointGain.Fixed(6))

        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        assertThat(preview.validations).contains(
            io.github.arhor.dnd.companion.domain.model.LevelUpValidationIssue(
                LevelUpValidationCode.MulticlassPrerequisite,
                "Ability prerequisites for Wizard are not met.",
                LevelUpValidationSeverity.Overrideable,
                "multiclass-prerequisite:wizard",
            ),
        )
        assertThat(preview.canConfirm).isFalse()
    }

    @Test
    fun `changing selected class invalidates only the affected acknowledgement`() {
        val sheet = CharacterSheet(
            id = "character",
            level = 1,
            experiencePoints = 0,
            abilityScores = AbilityScores(strength = 13, intelligence = 12),
        )
        val acknowledgements = setOf(
            "multiclass-prerequisite:wizard",
            "experience-threshold:2",
        )
        val wizardPlan = plan(
            expectedLevel = 1,
            classId = "wizard",
            hitPoints = HitPointGain.Fixed(6),
            selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(6),
                acknowledgedIssueCodes = acknowledgements,
            ),
        )

        val wizardPreview = LevelUpProgressionEngine.rebuild(sheet, progression("fighter"), wizardPlan, referenceData())
        assertThat(wizardPreview.requirements.filterIsInstance<io.github.arhor.dnd.companion.domain.model.LevelUpRequirement.Acknowledgement>()
            .map { it.id })
            .containsExactlyElementsIn(acknowledgements)

        val fighterPreview = LevelUpProgressionEngine.rebuild(
            sheet,
            progression("fighter"),
            wizardPlan.copy(selectedClassId = "fighter"),
            referenceData(),
        )
        val activeAcknowledgements = fighterPreview.requirements
            .filterIsInstance<io.github.arhor.dnd.companion.domain.model.LevelUpRequirement.Acknowledgement>()
            .map { it.id }
        assertThat(activeAcknowledgements).containsExactly("experience-threshold:2")
        assertThat(fighterPreview.canConfirm).isTrue()
    }

    @Test
    fun `rebuild should expose later subclass feature choices when a sticky subclass gains a feature`() {
        val championFeature = Feature(
            "champion-feature",
            "Champion feature",
            emptyList(),
            Choice.OptionsArrayChoice(1, listOf("a")),
        )
        val champion = io.github.arhor.dnd.companion.domain.model.Subclass(
            "champion",
            "Champion",
            emptyList(),
            subclassFlavor = "Martial",
            levels = listOf(ClassLevel("champion-3", 3, listOf(championFeature.id))),
        )
        val championFighter = fighter.copy(subclasses = listOf(champion))
        val existing = progression("fighter", "fighter").copy(
            levels = listOf(
                CharacterLevelRecord(1, "fighter", 1, hitPointGain = HitPointGain.Fixed(10)),
                CharacterLevelRecord(
                    2,
                    "fighter",
                    2,
                    subclassId = "champion",
                    hitPointGain = HitPointGain.Fixed(6),
                ),
            ),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            featureChoices = mapOf("champion-feature:choice" to setOf("a")),
        )
        val data = LevelUpReferenceData(
            listOf(championFighter, wizard),
            listOf(championFeature),
            referenceDataVersion = "test-v1",
        )

        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 2),
            existing,
            plan(2, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        assertThat(
            preview.requirements
                .filterIsInstance<io.github.arhor.dnd.companion.domain.model.LevelUpRequirement.ChoiceSelection>()
                .map { it.id },
        ).contains("champion-feature:choice")
    }

    @Test
    fun `rebuild should enforce feat proficiency prerequisites and ability score caps when selecting a feat`() {
        val feat = Feat(
            id = "strong-feat",
            name = "Strong feat",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.ProficiencyPrerequisite("skill-athletics")),
            abilityBonusChoice = Choice.AbilityBonusChoice(1, listOf(mapOf(AbilityIds.STR to 1))),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = io.github.arhor.dnd.companion.domain.model.AbilityScoreDecision.Feat(feat.id),
            featChoices = mapOf("strong-feat:ability-bonus" to setOf(AbilityIds.STR)),
        )
        val data = LevelUpReferenceData(listOf(fighter, wizard), emptyList(), listOf(feat), "test-v1")
        val proficientData = LevelUpReferenceData(
            listOf(fighter.copy(proficiencies = listOf("skill-athletics")), wizard),
            emptyList(),
            listOf(feat),
            "test-v1",
        )

        val missingProficiency = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )
        val capped = LevelUpProgressionEngine.rebuild(
            CharacterSheet(
                id = "character",
                level = 3,
                abilityScores = AbilityScores(strength = 20),
            ),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            proficientData,
        )

        assertThat(missingProficiency.validations.map { it.code })
            .contains(LevelUpValidationCode.FeatPrerequisite)
        assertThat(capped.validations.map { it.code })
            .contains(LevelUpValidationCode.InvalidAbilityScoreIncrease)
    }

    @Test
    fun `rebuild should expose per-class reason when multiclass prerequisite requires override`() {
        val sheet = CharacterSheet(
            id = "character",
            level = 1,
            abilityScores = AbilityScores(strength = 13, intelligence = 10),
        )

        val requirement = LevelUpProgressionEngine.rebuild(
            sheet,
            progression("fighter"),
            plan(1, "fighter", HitPointGain.Fixed(6)),
            referenceData(),
        ).requirements
            .filterIsInstance<io.github.arhor.dnd.companion.domain.model.LevelUpRequirement.ClassSelection>()
            .single()

        assertThat(requirement.eligibility.single { it.classId == "wizard" }.eligible).isFalse()
        assertThat(requirement.eligibility.single { it.classId == "wizard" }.reasons.single())
            .contains("override")
    }

    private fun plan(
        expectedLevel: Int,
        classId: String,
        hitPoints: HitPointGain,
        selections: LevelUpSelections = LevelUpSelections(hitPointGain = hitPoints),
    ) = LevelUpPlan(
        expectedTotalLevel = expectedLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = "test-v1",
        selectedClassId = classId,
        selections = selections,
    )

    private fun progression(vararg classIds: String): CharacterProgression = progression(classIds.toList())

    private fun progression(classIds: List<String>): CharacterProgression = CharacterProgression(
        referenceDataVersion = "test-v1",
        origin = ProgressionOrigin.Guided,
        levels = classIds.mapIndexed { index, classId ->
            CharacterLevelRecord(
                characterLevel = index + 1,
                classId = classId,
                classLevel = classIds.take(index + 1).count { it == classId },
                hitPointGain = HitPointGain.Fixed(if (index == 0) 10 else 6),
            )
        },
    )

    private fun referenceData() = LevelUpReferenceData(
        classes = listOf(fighter, wizard),
        features = emptyList(),
        referenceDataVersion = "test-v1",
    )

    private companion object {
        val fighter = characterClass(
            id = "fighter",
            hitDie = 10,
            prerequisites = listOf(AbilityScorePrerequisite(listOf(AbilityIds.STR), 13)),
        )
        val wizard = characterClass(
            id = "wizard",
            hitDie = 6,
            prerequisites = listOf(AbilityScorePrerequisite(listOf(AbilityIds.INT), 13)),
        )

        fun characterClass(
            id: String,
            hitDie: Int,
            prerequisites: List<AbilityScorePrerequisite>,
        ) = CharacterClass(
            id = id,
            name = id.replaceFirstChar { it.uppercase() },
            multiClassing = MultiClassing(prerequisites = prerequisites),
            hitDie = hitDie,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            subclasses = emptyList(),
            levels = (1..20).map { level -> ClassLevel("$id-$level", level, emptyList()) },
        )
    }
}
