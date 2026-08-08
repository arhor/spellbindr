package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelSpellcasting
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.Spellcasting
import com.github.arhor.spellbindr.domain.model.calculateSpellcastingClassStats
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpSpellcastingDerivedStatsTest {

    @Test
    fun `rebuild should update spell save dc and attack when proficiency bonus increases`() {
        val progression = progression("wizard", "wizard", "wizard", "wizard")
        val sheet = CharacterSheet(
            id = "character",
            level = 4,
            abilityScores = AbilityScores(intelligence = 16),
        )
        val data = referenceData()
        val preview = LevelUpProgressionEngine.rebuild(
            sheet = sheet,
            progression = progression,
            plan = wizardPlan(
                expectedLevel = 4,
                addedSpellIds = setOf("shield", "magic-missile"),
            ),
            referenceData = data,
        )

        assertThat(preview.canConfirm).isTrue()
        val before = preview.before.calculateSpellcastingClassStats(data.classes).getValue("wizard")
        val after = preview.after.calculateSpellcastingClassStats(data.classes).getValue("wizard")
        assertThat(before.abilityId).isEqualTo(AbilityIds.INT)
        assertThat(before.spellSaveDc).isEqualTo(13)
        assertThat(before.spellAttackBonus).isEqualTo(5)
        assertThat(after.spellSaveDc).isEqualTo(14)
        assertThat(after.spellAttackBonus).isEqualTo(6)
    }

    @Test
    fun `rebuild should keep multiclass spellcasting abilities separate when one ability changes`() {
        val progression = progression("wizard", "cleric", "wizard", "wizard")
        val sheet = CharacterSheet(
            id = "character",
            level = 4,
            abilityScores = AbilityScores(intelligence = 16, wisdom = 14),
        )
        val data = referenceData()
        val preview = LevelUpProgressionEngine.rebuild(
            sheet = sheet,
            progression = progression,
            plan = wizardPlan(
                expectedLevel = 4,
                addedSpellIds = setOf("shield", "magic-missile"),
                abilityScoreDecision = AbilityScoreDecision.Increase(mapOf(AbilityIds.INT to 2)),
            ),
            referenceData = data,
        )

        assertThat(preview.canConfirm).isTrue()
        val before = preview.before.calculateSpellcastingClassStats(data.classes)
        val after = preview.after.calculateSpellcastingClassStats(data.classes)

        assertThat(before.getValue("wizard").abilityId).isEqualTo(AbilityIds.INT)
        assertThat(before.getValue("cleric").abilityId).isEqualTo(AbilityIds.WIS)
        assertThat(before.getValue("wizard").spellSaveDc).isEqualTo(13)
        assertThat(before.getValue("wizard").spellAttackBonus).isEqualTo(5)
        assertThat(before.getValue("cleric").spellSaveDc).isEqualTo(12)
        assertThat(before.getValue("cleric").spellAttackBonus).isEqualTo(4)

        assertThat(after.getValue("wizard").spellSaveDc).isEqualTo(15)
        assertThat(after.getValue("wizard").spellAttackBonus).isEqualTo(7)
        assertThat(after.getValue("cleric").spellSaveDc).isEqualTo(13)
        assertThat(after.getValue("cleric").spellAttackBonus).isEqualTo(5)
    }

    private fun wizardPlan(
        expectedLevel: Int,
        addedSpellIds: Set<String>,
        abilityScoreDecision: AbilityScoreDecision? = null,
    ): LevelUpPlan = LevelUpPlan(
        expectedTotalLevel = expectedLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = REFERENCE_VERSION,
        selectedClassId = "wizard",
        selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(4),
            abilityScoreDecision = abilityScoreDecision,
            spellChanges = SpellChanges(
                addedToSpellbook = addedSpellIds.mapTo(linkedSetOf()) { spellId ->
                    ClassSpellRef("wizard", spellId)
                },
            ),
        ),
    )

    private fun progression(vararg classIds: String): CharacterProgression = CharacterProgression(
        referenceDataVersion = REFERENCE_VERSION,
        origin = ProgressionOrigin.Guided,
        levels = classIds.mapIndexed { index, classId ->
            CharacterLevelRecord(
                characterLevel = index + 1,
                classId = classId,
                classLevel = classIds.take(index + 1).count { it == classId },
                hitPointGain = HitPointGain.Fixed(if (index == 0) 6 else 4),
            )
        },
    )

    private fun referenceData(): LevelUpReferenceData = LevelUpReferenceData(
        classes = listOf(
            casterClass("wizard", AbilityIds.INT, includeLevelSpellcasting = true),
            casterClass("cleric", AbilityIds.WIS),
        ),
        features = emptyList(),
        spells = listOf("shield", "magic-missile").map(::wizardSpell),
        referenceDataVersion = REFERENCE_VERSION,
    )

    private fun casterClass(
        id: String,
        abilityId: String,
        includeLevelSpellcasting: Boolean = false,
    ): CharacterClass = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        multiClassing = MultiClassing(),
        hitDie = 6,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        spellcasting = Spellcasting(
            info = emptyList(),
            level = 1,
            spellcastingAbility = EntityRef(abilityId),
        ),
        subclasses = emptyList(),
        levels = (1..20).map { level ->
            ClassLevel(
                id = "$id-$level",
                level = level,
                features = emptyList(),
                spellcasting = if (includeLevelSpellcasting) {
                    LevelSpellcasting(
                        cantrips = 0,
                        spells = 0,
                        spellSlots = mapOf("1" to 2),
                    )
                } else {
                    null
                },
            )
        },
    )

    private fun wizardSpell(id: String): Spell = Spell(
        id = id,
        name = id,
        desc = emptyList(),
        level = 1,
        range = "Self",
        ritual = false,
        school = EntityRef("abjuration"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = listOf(EntityRef("wizard")),
        components = emptyList(),
        concentration = false,
        source = "test",
    )

    private companion object {
        private const val REFERENCE_VERSION = "test-v1"
    }
}
