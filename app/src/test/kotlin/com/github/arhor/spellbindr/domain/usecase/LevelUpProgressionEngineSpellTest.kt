package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScorePrerequisite
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineSpellTest {

    @Test
    fun `rebuild should calculate shared and pact magic slots separately when wizard gains a level`() {
        val warlock = characterClass("warlock", 8, emptyList())
        val progression = progression("wizard", "warlock")
        val sheet = CharacterSheet(id = "character", level = 2)
        val plan = plan(2, "wizard", HitPointGain.Fixed(4))
        val data = LevelUpReferenceData(
            listOf(fighter, wizard, warlock),
            emptyList(),
            referenceDataVersion = "test-v1",
        )

        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, data)

        assertThat(preview.before.sharedSpellSlots).containsExactly(1, 2)
        assertThat(preview.before.pactMagic?.slotLevel).isEqualTo(1)
        assertThat(preview.before.pactMagic?.slots).isEqualTo(1)
        assertThat(preview.after.sharedSpellSlots).containsExactly(1, 3)
        assertThat(preview.after.pactMagic?.slotLevel).isEqualTo(1)
        assertThat(preview.after.pactMagic?.slots).isEqualTo(1)
    }

    @Test
    fun `rebuild should reject a spell that is not on the selected class list when choosing a known spell`() {
        val bard = characterClass("bard", 8, emptyList()).copy(
            levels = (1..20).map { level ->
                ClassLevel(
                    "bard-$level",
                    level,
                    emptyList(),
                    if (level == 1) {
                        com.github.arhor.spellbindr.domain.model.LevelSpellcasting(
                            2,
                            4,
                            mapOf("1" to 2),
                        )
                    } else {
                        null
                    },
                )
            },
        )
        val spell = Spell(
            "wizard-only",
            "Wizard only",
            emptyList(),
            1,
            "Self",
            false,
            EntityRef("evocation"),
            "Instant",
            "1 action",
            listOf(EntityRef("wizard")),
            emptyList(),
            false,
            source = "SRD",
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(8),
            spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", spell.id))),
        )
        val data = LevelUpReferenceData(
            listOf(fighter, wizard, bard),
            emptyList(),
            spells = listOf(spell),
            referenceDataVersion = "test-v1",
        )

        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 0),
            CharacterProgression(
                "srd-5e-2014-v1",
                "test-v1",
                ProgressionOrigin.Guided,
                emptyList(),
            ),
            plan(0, "bard", HitPointGain.Fixed(8), selections),
            data,
        )

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should reject replacement when removed spell is not in reconstructed class spell state`() {
        val bard = characterClass("bard", 8, emptyList()).copy(
            levels = (1..20).map { level ->
                ClassLevel(
                    "bard-$level",
                    level,
                    emptyList(),
                    com.github.arhor.spellbindr.domain.model.LevelSpellcasting(
                        2,
                        if (level == 1) 2 else 3,
                        mapOf("1" to 2),
                    ),
                )
            },
        )
        fun bardSpell(id: String) = Spell(
            id,
            id,
            emptyList(),
            1,
            "Self",
            false,
            EntityRef("evocation"),
            "Instant",
            "1 action",
            listOf(EntityRef("bard")),
            emptyList(),
            false,
            source = "SRD",
        )
        val existing = progression("bard").copy(
            levels = listOf(
                progression("bard").levels.single().copy(
                    spellChanges = SpellChanges(
                        learned = setOf(ClassSpellRef("bard", "known")),
                    ),
                ),
            ),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(5),
            spellChanges = SpellChanges(
                learned = setOf(ClassSpellRef("bard", "level-gain")),
                replaced = setOf(SpellReplacement("bard", "not-known", "replacement")),
            ),
        )
        val data = LevelUpReferenceData(
            listOf(fighter, wizard, bard),
            emptyList(),
            spells = listOf(
                bardSpell("known"),
                bardSpell("level-gain"),
                bardSpell("replacement"),
            ),
            referenceDataVersion = "test-v1",
        )

        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 1),
            existing,
            plan(1, "bard", HitPointGain.Fixed(5), selections),
            data,
        )

        assertThat(preview.validations.map { it.message }).contains(
            "Replacements must remove a currently known class spell and learn a distinct, non-duplicate spell.",
        )
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
