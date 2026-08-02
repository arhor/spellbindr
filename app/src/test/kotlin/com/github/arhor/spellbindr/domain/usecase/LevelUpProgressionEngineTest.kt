package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScorePrerequisite
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.Prerequisite
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineTest {

    @Test
    fun `rebuild should derive the next level from ordered class history when returning to an earlier class`() {
        // Given
        val progression = progression("fighter", "wizard", "fighter")
        val sheet = CharacterSheet(id = "character", level = 3)
        val plan = plan(expectedLevel = 3, classId = "fighter", hitPoints = HitPointGain.Fixed(6))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.after.classLevels).containsExactly("fighter", 3, "wizard", 1)
        assertThat(preview.after.totalLevel).isEqualTo(4)
    }

    @Test
    fun `rebuild should require acknowledgement when a new class multiclass prerequisite is not met`() {
        // Given
        val progression = progression("fighter")
        val sheet = CharacterSheet(id = "character", level = 1, abilityScores = AbilityScores(strength = 13, intelligence = 12))
        val plan = plan(expectedLevel = 1, classId = "wizard", hitPoints = HitPointGain.Fixed(6))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations).contains(
            issue(LevelUpValidationCode.MulticlassPrerequisite, LevelUpValidationSeverity.Overrideable),
        )
        assertThat(preview.canConfirm).isFalse()
    }

    @Test
    fun `rebuild should block a level up when the character is already level twenty`() {
        // Given
        val progression = progression(List(20) { "fighter" })
        val sheet = CharacterSheet(id = "character", level = 20)
        val plan = plan(expectedLevel = 20, classId = "fighter", hitPoints = HitPointGain.Fixed(6))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.MaximumCharacterLevel)
        assertThat(preview.canConfirm).isFalse()
    }

    @Test
    fun `rebuild should apply constitution improvements to every hit die when an ASI raises constitution`() {
        // Given
        val progression = progression(List(3) { "fighter" })
        val sheet = CharacterSheet(id = "character", level = 3, abilityScores = AbilityScores(constitution = 10))
        val plan = plan(
            expectedLevel = 3,
            classId = "fighter",
            hitPoints = HitPointGain.Fixed(6),
            selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(6),
                abilityScoreDecision = com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Increase(
                    mapOf(AbilityIds.CON to 2),
                ),
            ),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.after.maximumHitPoints - preview.before.maximumHitPoints).isEqualTo(10)
        assertThat(preview.after.abilityScores.constitution).isEqualTo(12)
    }

    @Test
    fun `rebuild should produce equal previews when inputs are unchanged`() {
        // Given
        val progression = progression("fighter")
        val sheet = CharacterSheet(id = "character", level = 1)
        val plan = plan(expectedLevel = 1, classId = "fighter", hitPoints = HitPointGain.Fixed(6))

        // When
        val previews = List(2) { LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData()) }

        // Then
        assertThat(previews[0]).isEqualTo(previews[1])
    }

    @Test
    fun `rebuild should block confirmation when no class has been selected`() {
        // Given
        val plan = LevelUpPlan(1, CharacterProgression.SUPPORTED_RULESET_ID, "test-v1")

        // When
        val preview = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 1), progression("fighter"), plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.ChoiceRequired)
    }

    @Test
    fun `rebuild should block a subclass selected before its acquisition level`() {
        // Given
        val plan = plan(1, "fighter", HitPointGain.Fixed(6), LevelUpSelections(subclassId = "champion", hitPointGain = HitPointGain.Fixed(6)))

        // When
        val preview = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 1), progression("fighter"), plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.StickySubclass)
    }

    @Test
    fun `rebuild should expose later subclass feature choices when a sticky subclass gains a feature`() {
        // Given
        val championFeature = Feature("champion-feature", "Champion feature", emptyList(), Choice.OptionsArrayChoice(1, listOf("a")))
        val champion = com.github.arhor.spellbindr.domain.model.Subclass("champion", "Champion", emptyList(), subclassFlavor = "Martial", levels = listOf(ClassLevel("champion-3", 3, listOf(championFeature.id))))
        val championFighter = fighter.copy(subclasses = listOf(champion))
        val existing = progression("fighter", "fighter").copy(levels = listOf(
            CharacterLevelRecord(1, "fighter", 1, hitPointGain = HitPointGain.Fixed(10)),
            CharacterLevelRecord(2, "fighter", 2, subclassId = "champion", hitPointGain = HitPointGain.Fixed(6)),
        ))
        val plan = plan(2, "fighter", HitPointGain.Fixed(6), LevelUpSelections(hitPointGain = HitPointGain.Fixed(6), featureChoices = mapOf("champion-feature:choice" to setOf("a"))))

        // When
        val preview = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 2), existing, plan, LevelUpReferenceData(listOf(championFighter, wizard), listOf(championFeature), referenceDataVersion = "test-v1"))

        // Then
        assertThat(preview.requirements.filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.ChoiceSelection>().map { it.id }).contains("champion-feature:choice")
    }

    @Test
    fun `rebuild should enforce feat proficiency prerequisites and ability score caps when selecting a feat`() {
        // Given
        val feat = Feat(
            id = "strong-feat",
            name = "Strong feat",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.ProficiencyPrerequisite("skill-athletics")),
            abilityBonusChoice = Choice.AbilityBonusChoice(1, listOf(mapOf(AbilityIds.STR to 1))),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = com.github.arhor.spellbindr.domain.model.AbilityScoreDecision.Feat(feat.id),
            featChoices = mapOf("strong-feat:ability-bonus" to setOf(AbilityIds.STR)),
        )
        val data = LevelUpReferenceData(listOf(fighter, wizard), emptyList(), listOf(feat), "test-v1")
        val proficientData = LevelUpReferenceData(listOf(fighter.copy(proficiencies = listOf("skill-athletics")), wizard), emptyList(), listOf(feat), "test-v1")

        // When
        val missingProficiency = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 3), progression(List(3) { "fighter" }), plan(3, "fighter", HitPointGain.Fixed(6), selections), data)
        val capped = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 3, abilityScores = AbilityScores(strength = 20)), progression(List(3) { "fighter" }), plan(3, "fighter", HitPointGain.Fixed(6), selections), proficientData)

        // Then
        assertThat(missingProficiency.validations.map { it.code }).contains(LevelUpValidationCode.FeatPrerequisite)
        assertThat(capped.validations.map { it.code }).contains(LevelUpValidationCode.InvalidAbilityScoreIncrease)
    }

    @Test
    fun `rebuild should calculate shared and pact magic slots separately when the character has wizard and warlock levels`() {
        // Given
        val warlock = characterClass("warlock", 8, emptyList())
        val progression = progression("wizard", "warlock", "wizard")
        val sheet = CharacterSheet(id = "character", level = 3)
        val plan = plan(3, "wizard", HitPointGain.Fixed(4))
        val data = LevelUpReferenceData(listOf(fighter, wizard, warlock), emptyList(), referenceDataVersion = "test-v1")

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, data)

        // Then
        assertThat(preview.before.sharedSpellSlots).containsExactly(1, 3)
        assertThat(preview.before.pactMagic?.slotLevel).isEqualTo(1)
        assertThat(preview.before.pactMagic?.slots).isEqualTo(1)
    }

    @Test
    fun `rebuild should reject a spell that is not on the selected class list when choosing a known spell`() {
        // Given
        val bard = characterClass("bard", 8, emptyList()).copy(levels = (1..20).map { level ->
            ClassLevel("bard-$level", level, emptyList(), if (level == 1) com.github.arhor.spellbindr.domain.model.LevelSpellcasting(2, 4, mapOf("1" to 2)) else null)
        })
        val spell = Spell("wizard-only", "Wizard only", emptyList(), 1, "Self", false, EntityRef("evocation"), "Instant", "1 action", listOf(EntityRef("wizard")), emptyList(), false, source = "SRD")
        val selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(8), spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", spell.id))))
        val data = LevelUpReferenceData(listOf(fighter, wizard, bard), emptyList(), spells = listOf(spell), referenceDataVersion = "test-v1")

        // When
        val preview = LevelUpProgressionEngine.rebuild(CharacterSheet(id = "character", level = 0), CharacterProgression("srd-5e-2014-v1", "test-v1", ProgressionOrigin.Guided, emptyList()), plan(0, "bard", HitPointGain.Fixed(8), selections), data)

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should reject replacement when removed spell is not in reconstructed class spell state`() {
        // Given
        val bard = characterClass("bard", 8, emptyList()).copy(levels = (1..20).map { level ->
            ClassLevel("bard-$level", level, emptyList(), com.github.arhor.spellbindr.domain.model.LevelSpellcasting(2, if (level == 1) 2 else 3, mapOf("1" to 2)))
        })
        fun bardSpell(id: String) = Spell(id, id, emptyList(), 1, "Self", false, EntityRef("evocation"),
            "Instant", "1 action", listOf(EntityRef("bard")), emptyList(), false, source = "SRD")
        val existing = progression("bard").copy(levels = listOf(
            progression("bard").levels.single().copy(
                spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", "known"))),
            ),
        ))
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(5),
            spellChanges = SpellChanges(
                learned = setOf(ClassSpellRef("bard", "level-gain")),
                replaced = setOf(SpellReplacement("bard", "not-known", "replacement")),
            ),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 1), existing,
            plan(1, "bard", HitPointGain.Fixed(5), selections),
            LevelUpReferenceData(listOf(fighter, wizard, bard), emptyList(), spells =
                listOf(bardSpell("known"), bardSpell("level-gain"), bardSpell("replacement")), referenceDataVersion = "test-v1"),
        )

        // Then
        assertThat(preview.validations.map { it.message }).contains(
            "Replacements must remove a currently known class spell and learn a distinct, non-duplicate spell.",
        )
    }

    @Test
    fun `rebuild should expose per-class reason when multiclass prerequisite requires override`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1, abilityScores = AbilityScores(strength = 13, intelligence = 10))

        // When
        val requirement = LevelUpProgressionEngine.rebuild(
            sheet, progression("fighter"), plan(1, "fighter", HitPointGain.Fixed(6)), referenceData(),
        ).requirements.filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.ClassSelection>().single()

        // Then
        assertThat(requirement.eligibility.single { it.classId == "wizard" }.eligible).isFalse()
        assertThat(requirement.eligibility.single { it.classId == "wizard" }.reasons.single()).contains("override")
    }

    private fun issue(code: LevelUpValidationCode, severity: LevelUpValidationSeverity) =
        com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue(code, "Ability prerequisites for Wizard are not met.", severity)

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
