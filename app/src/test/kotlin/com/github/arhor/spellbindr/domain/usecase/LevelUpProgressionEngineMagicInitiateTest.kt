package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.Spell
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineMagicInitiateTest {

    @Test
    fun `rebuild should filter Magic Initiate choices by selected class list and spell level`() {
        val preview = LevelUpProgressionEngine.rebuild(
            sheet(),
            progression(),
            plan(mapOf(CLASS_LIST to setOf("wizard"))),
            referenceData(),
        )

        val choices = preview.requirements
            .filterIsInstance<LevelUpRequirement.ChoiceSelection>()
            .associateBy { it.id }

        assertThat(choices.getValue(CLASS_LIST).options.map { it.id }).containsExactly("wizard")
        assertThat(choices.getValue(CANTRIPS).options.map { it.id })
            .containsExactly("fire-bolt", "mage-hand")
        assertThat(choices.getValue(FIRST_LEVEL).options.map { it.id })
            .containsExactly("magic-missile", "shield")
        assertThat(choices.getValue(CANTRIPS).options.map { it.id }).doesNotContain("sacred-flame")
        assertThat(choices.getValue(FIRST_LEVEL).options.map { it.id }).doesNotContain("misty-step")
    }

    @Test
    fun `rebuild should block illegal Magic Initiate spell selections`() {
        val preview = LevelUpProgressionEngine.rebuild(
            sheet(),
            progression(),
            plan(mapOf(
                CLASS_LIST to setOf("wizard"),
                CANTRIPS to setOf("fire-bolt", "sacred-flame"),
                FIRST_LEVEL to setOf("magic-missile"),
            )),
            referenceData(),
        )

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidChoice)
        assertThat(preview.canConfirm).isFalse()
    }

    @Test
    fun `recordFor should retain Magic Initiate choices and feat owned spell refs`() {
        val data = referenceData()
        val plan = plan(mapOf(
            CLASS_LIST to setOf("wizard"),
            CANTRIPS to setOf("fire-bolt", "mage-hand"),
            FIRST_LEVEL to setOf("magic-missile"),
        ))
        val preview = LevelUpProgressionEngine.rebuild(sheet(), progression(), plan, data)

        val record = LevelUpProgressionEngine.recordFor(
            plan = plan,
            clazz = data.classesById.getValue("fighter"),
            classLevel = 4,
            progression = progression(),
            referenceData = data,
            validations = preview.validations,
        )

        assertThat(preview.canConfirm).isTrue()
        assertThat(record.featChoices).containsExactlyEntriesIn(plan.selections.featChoices)
        assertThat(record.spellChanges.featureLearned.getValue("feat:magic-initiate"))
            .containsExactly(
                com.github.arhor.spellbindr.domain.model.ClassSpellRef("wizard", "fire-bolt"),
                com.github.arhor.spellbindr.domain.model.ClassSpellRef("wizard", "mage-hand"),
                com.github.arhor.spellbindr.domain.model.ClassSpellRef("wizard", "magic-missile"),
            )
    }

    private fun sheet() = CharacterSheet(id = "character", level = 3, className = "Fighter 3")

    private fun progression() = CharacterProgression(
        referenceDataVersion = "test-v1",
        origin = ProgressionOrigin.Guided,
        levels = (1..3).map { level ->
            CharacterLevelRecord(
                characterLevel = level,
                classId = "fighter",
                classLevel = level,
                hitPointGain = HitPointGain.Fixed(if (level == 1) 10 else 6),
            )
        },
    )

    private fun plan(featChoices: Map<String, Set<String>>) = LevelUpPlan(
        expectedTotalLevel = 3,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = "test-v1",
        selectedClassId = "fighter",
        selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat("magic-initiate"),
            featChoices = featChoices,
        ),
    )

    private fun referenceData() = LevelUpReferenceData(
        classes = listOf(characterClass("fighter", 10), characterClass("wizard", 6)),
        features = emptyList(),
        feats = listOf(Feat("magic-initiate", "Magic Initiate", emptyList())),
        spells = listOf(
            spell("fire-bolt", "Fire Bolt", 0, "wizard"),
            spell("mage-hand", "Mage Hand", 0, "wizard"),
            spell("magic-missile", "Magic Missile", 1, "wizard"),
            spell("shield", "Shield", 1, "wizard"),
            spell("misty-step", "Misty Step", 2, "wizard"),
            spell("sacred-flame", "Sacred Flame", 0, "cleric"),
        ),
        referenceDataVersion = "test-v1",
    )

    private fun characterClass(id: String, hitDie: Int) = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        hitDie = hitDie,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = (1..20).map { level -> ClassLevel("$id-$level", level, emptyList()) },
    )

    private fun spell(id: String, name: String, level: Int, classId: String) = Spell(
        id = id,
        name = name,
        desc = emptyList(),
        level = level,
        range = "Self",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = listOf(EntityRef(classId)),
        components = emptyList(),
        concentration = false,
        source = "test",
    )

    private companion object {
        const val CLASS_LIST = "magic-initiate:class-list"
        const val CANTRIPS = "magic-initiate:cantrips"
        const val FIRST_LEVEL = "magic-initiate:first-level-spell"
    }
}
