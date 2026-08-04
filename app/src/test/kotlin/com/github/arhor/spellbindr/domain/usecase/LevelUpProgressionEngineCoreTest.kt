package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScorePrerequisite
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineCoreTest {

    @Test
    fun `rebuild should derive the next level from ordered class history when returning to an earlier class`() {
        val progression = progression("fighter", "wizard", "fighter")
        val sheet = CharacterSheet(id = "character", level = 3)
        val plan = plan(3, "fighter", HitPointGain.Fixed(6))

        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        assertThat(preview.after.classLevels).containsExactly("fighter", 3, "wizard", 1)
        assertThat(preview.after.totalLevel).isEqualTo(4)
    }

    @Test
    fun `rebuild should block a level up when the character is already level twenty`() {
        val progression = progression(List(20) { "fighter" })
        val sheet = CharacterSheet(id = "character", level = 20)
        val plan = plan(20, "fighter", HitPointGain.Fixed(6))

        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.MaximumCharacterLevel)
        assertThat(preview.canConfirm).isFalse()
    }

    @Test
    fun `rebuild should apply constitution improvements to every hit die when an ASI raises constitution`() {
        val progression = progression(List(3) { "fighter" })
        val sheet = CharacterSheet(id = "character", level = 3, abilityScores = AbilityScores(constitution = 10))
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Increase(
                mapOf(AbilityIds.CON to 2),
            ),
        )
        val plan = plan(3, "fighter", HitPointGain.Fixed(6), selections)

        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        assertThat(preview.after.maximumHitPoints - preview.before.maximumHitPoints).isEqualTo(10)
        assertThat(preview.after.abilityScores.constitution).isEqualTo(12)
    }

    @Test
    fun `rebuild should produce equal previews when inputs are unchanged`() {
        val progression = progression("fighter")
        val sheet = CharacterSheet(id = "character", level = 1)
        val plan = plan(1, "fighter", HitPointGain.Fixed(6))

        val previews = List(2) { LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData()) }

        assertThat(previews[0]).isEqualTo(previews[1])
    }

    @Test
    fun `rebuild should block confirmation when no class has been selected`() {
        val plan = LevelUpPlan(1, CharacterProgression.SUPPORTED_RULESET_ID, "test-v1")

        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 1),
            progression("fighter"),
            plan,
            referenceData(),
        )

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.ChoiceRequired)
    }

    @Test
    fun `rebuild should block a subclass selected before its acquisition level`() {
        val selections = LevelUpSelections(
            subclassId = "champion",
            hitPointGain = HitPointGain.Fixed(6),
        )
        val plan = plan(1, "fighter", HitPointGain.Fixed(6), selections)

        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 1),
            progression("fighter"),
            plan,
            referenceData(),
        )

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.StickySubclass)
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
            prrequisites = listOf(AbilityScorePrerequisite(listOf(AbilityIds.INT), 13)),
        )

        fun characterClass(
            id: String,
            hitDie: Int,
            prerequisites: List<AbilityScorePrerequisite>,
        ) = CharacterClass(
            id = id,
            name = id.replaceFirstChar { it.uppercase() },
            multiClassing = MultiClassing(prerequisites = prrequisites),
            hitDie = hitDie,
            proficiencies = emptyList(),
            proficiencyChoices = emptyList(),
            savingThrows = emptyList(),
            subclasses = emptyList(),
            levels = (1..20).map { level -> ClassLevel("$id-$level", level, emptyList()) },
        )
    }
}
