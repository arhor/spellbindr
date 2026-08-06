package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelSpellcasting
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.MultiClassing
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement
import com.github.arhor.spellbindr.domain.model.Subclass
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpSpellDecisionsTest {

    @Test
    fun `rebuild should expose class scoped known spell candidates when multiclass slots are higher`() {
        // Given
        val bard = casterClass("bard", 8) { level ->
            LevelSpellcasting(cantrips = 2, spells = if (level == 1) 2 else 3, spellSlots = mapOf("1" to 3))
        }
        val wizard = casterClass("wizard", 6) { level ->
            LevelSpellcasting(cantrips = 3, spellSlots = mapOf("1" to 3, "2" to 2))
        }
        val progression = progression(
            record(
                characterLevel = 1,
                classId = "wizard",
                classLevel = 1,
                spellChanges = SpellChanges(learned = setOf(ClassSpellRef("wizard", "shared-other-class"))),
            ),
            record(
                characterLevel = 2,
                classId = "bard",
                classLevel = 1,
                spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", "bard-known"))),
            ),
        )
        val spells = listOf(
            spell("bard-known", 1, "bard"),
            spell("shared-other-class", 1, "bard", "wizard"),
            spell("too-high-for-bard", 2, "bard"),
            spell("wizard-only", 1, "wizard"),
        )

        // When
        val requirement = rebuild(
            sheet = CharacterSheet(id = "character", level = 2),
            progression = progression,
            plan = plan(2, "bard", SpellChanges(learned = setOf(ClassSpellRef("bard", "shared-other-class")))),
            classes = listOf(bard, wizard),
            spells = spells,
        ).requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()

        // Then
        assertThat(requirement.requiredKnownSpellCount).isEqualTo(1)
        assertThat(requirement.knownSpellCandidates.map { it.spellId }).containsExactly("shared-other-class")
    }

    @Test
    fun `rebuild should accept completed replacement when source is a currently known class spell`() {
        // Given
        val bard = casterClass("bard", 8) { level ->
            LevelSpellcasting(cantrips = 2, spells = if (level == 1) 2 else 3, spellSlots = mapOf("1" to 3))
        }
        val progression = progression(record(
            characterLevel = 1,
            classId = "bard",
            classLevel = 1,
            spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", "known"))),
        ))
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "level-gain")),
            replaced = setOf(SpellReplacement("bard", "known", "replacement")),
        )

        // When
        val preview = rebuild(
            sheet = CharacterSheet(id = "character", level = 1),
            progression = progression,
            plan = plan(1, "bard", changes),
            classes = listOf(bard),
            spells = listOf(spell("known", 1, "bard"), spell("level-gain", 1, "bard"), spell("replacement", 1, "bard")),
        )

        // Then
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.SpellPolicy)
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()
        assertThat(requirement.replacement?.selectedSourceSpellId).isEqualTo("known")
        assertThat(requirement.replacement?.selectedReplacementSpellId).isEqualTo("replacement")
    }

    @Test
    fun `rebuild should reject pending replacement when target spell is unresolved`() {
        // Given
        val bard = casterClass("bard", 8) { level ->
            LevelSpellcasting(cantrips = 2, spells = if (level == 1) 2 else 3, spellSlots = mapOf("1" to 3))
        }
        val progression = progression(record(
            characterLevel = 1,
            classId = "bard",
            classLevel = 1,
            spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", "known"))),
        ))
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "level-gain")),
            replacementSourceSpellId = "known",
        )

        // When
        val preview = rebuild(
            sheet = CharacterSheet(id = "character", level = 1),
            progression = progression,
            plan = plan(1, "bard", changes),
            classes = listOf(bard),
            spells = listOf(spell("known", 1, "bard"), spell("level-gain", 1, "bard")),
        )

        // Then
        assertThat(preview.validations.map { it.message })
            .contains("Choose the replacement spell or clear the optional replacement.")
    }

    @Test
    fun `rebuild should reject replacement when target belongs to another class`() {
        // Given
        val bard = casterClass("bard", 8) { level ->
            LevelSpellcasting(cantrips = 2, spells = if (level == 1) 2 else 3, spellSlots = mapOf("1" to 3))
        }
        val progression = progression(record(
            characterLevel = 1,
            classId = "bard",
            classLevel = 1,
            spellChanges = SpellChanges(learned = setOf(ClassSpellRef("bard", "known"))),
        ))
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "level-gain")),
            replaced = setOf(SpellReplacement("bard", "known", "wizard-only")),
        )

        // When
        val preview = rebuild(
            CharacterSheet(id = "character", level = 1),
            progression,
            plan(1, "bard", changes),
            listOf(bard),
            listOf(spell("known", 1, "bard"), spell("level-gain", 1, "bard"), spell("wizard-only", 1, "wizard")),
        )

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should reject replacement source when spell exists only in mutable sheet state`() {
        // Given
        val bard = casterClass("bard", 8) { level ->
            LevelSpellcasting(cantrips = 2, spells = if (level == 1) 2 else 3, spellSlots = mapOf("1" to 3))
        }
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "level-gain")),
            replacementSourceSpellId = "manual-spell",
        )

        // When
        val preview = rebuild(
            CharacterSheet(
                id = "character",
                level = 1,
                characterSpells = listOf(CharacterSpell("manual-spell", "bard")),
            ),
            progression(record(1, "bard", 1)),
            plan(1, "bard", changes),
            listOf(bard),
            listOf(spell("manual-spell", 1, "bard"), spell("level-gain", 1, "bard")),
        )

        // Then
        assertThat(preview.validations.map { it.message })
            .contains("The selected spell replacement source is not legal for this class level.")
    }

    @Test
    fun `rebuild should reject duplicate spell when ordinary and feature grants overlap`() {
        // Given
        val bard = casterClass(
            id = "bard",
            hitDie = 8,
            levelFeatures = mapOf(10 to listOf("magical-secrets-1")),
        ) { level ->
            LevelSpellcasting(
                cantrips = if (level < 10) 3 else 4,
                spells = if (level < 10) 12 else 14,
                spellSlots = mapOf("1" to 4, "5" to 2),
            )
        }
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "duplicate")),
            featureLearned = mapOf("magical-secrets-1" to setOf(
                ClassSpellRef("bard", "duplicate"),
                ClassSpellRef("bard", "secret"),
            )),
        )

        // When
        val preview = rebuild(
            CharacterSheet(id = "character", level = 9),
            bardProgression(9),
            plan(9, "bard", changes),
            listOf(bard),
            listOf(spell("duplicate", 0, "bard"), spell("secret", 5, "wizard")),
            listOf(Feature("magical-secrets-1", "Magical Secrets", emptyList())),
        )

        // Then
        assertThat(preview.validations.map { it.message }).contains("Each newly granted spell must be distinct.")
    }

    @Test
    fun `rebuild should expose cantrip choices and preparation capacity when prepared caster gains a level`() {
        // Given
        val druid = casterClass("druid", 8) {
            LevelSpellcasting(cantrips = 2, spellSlots = mapOf("1" to 2))
        }
        val cantrips = listOf(spell("druidcraft", 0, "druid"), spell("guidance", 0, "druid"))

        // When
        val requirement = rebuild(
            sheet = CharacterSheet(id = "character", level = 0, abilityScores = AbilityScores(wisdom = 16)),
            progression = progression(),
            plan = plan(
                expectedLevel = 0,
                classId = "druid",
                spellChanges = SpellChanges(learned = cantrips.mapTo(linkedSetOf()) { ClassSpellRef("druid", it.id) }),
                hitPointGain = HitPointGain.Fixed(8),
            ),
            classes = listOf(druid),
            spells = cantrips,
        ).requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()

        // Then
        assertThat(requirement.requiredCantripCount).isEqualTo(2)
        assertThat(requirement.cantripCandidates.map { it.spellId }).containsExactly("druidcraft", "guidance")
        assertThat(requirement.requiredKnownSpellCount).isEqualTo(0)
        assertThat(requirement.preparationCapacity).isEqualTo(4)
    }

    @Test
    fun `rebuild should expose exact spellbook additions when wizard takes first class level`() {
        // Given
        val wizard = casterClass("wizard", 6) {
            LevelSpellcasting(cantrips = 0, spellSlots = mapOf("1" to 2))
        }
        val bookSpells = (1..6).map { index -> spell("wizard-spell-$index", 1, "wizard") }
        val changes = SpellChanges(
            addedToSpellbook = bookSpells.mapTo(linkedSetOf()) { ClassSpellRef("wizard", it.id) },
        )

        // When
        val preview = rebuild(
            sheet = CharacterSheet(id = "character", level = 0, abilityScores = AbilityScores(intelligence = 16)),
            progression = progression(),
            plan = plan(0, "wizard", changes, HitPointGain.Fixed(6)),
            classes = listOf(wizard),
            spells = bookSpells,
        )

        // Then
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()
        assertThat(requirement.requiredSpellbookAdditionCount).isEqualTo(6)
        assertThat(requirement.spellbookCandidates.map { it.spellId })
            .containsExactlyElementsIn(bookSpells.map { it.id })
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should expose Magical Secrets separately when Bard reaches level ten`() {
        // Given
        val bard = casterClass(
            id = "bard",
            hitDie = 8,
            levelFeatures = mapOf(10 to listOf("magical-secrets-1")),
        ) { level ->
            LevelSpellcasting(
                cantrips = if (level < 10) 3 else 4,
                spells = if (level < 10) 12 else 14,
                spellSlots = mapOf("1" to 4, "5" to 2),
            )
        }
        val spells = listOf(
            spell("bard-cantrip", 0, "bard"),
            spell("wizard-cantrip", 0, "wizard"),
            spell("cleric-secret", 5, "cleric"),
        )
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "bard-cantrip")),
            featureLearned = mapOf("magical-secrets-1" to setOf(
                ClassSpellRef("bard", "wizard-cantrip"),
                ClassSpellRef("bard", "cleric-secret"),
            )),
        )

        // When
        val preview = rebuild(
            sheet = CharacterSheet(id = "character", level = 9),
            progression = bardProgression(9),
            plan = plan(9, "bard", changes),
            classes = listOf(bard),
            spells = spells,
            features = listOf(Feature("magical-secrets-1", "Magical Secrets", emptyList())),
        )

        // Then
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()
        assertThat(requirement.requiredKnownSpellCount).isEqualTo(0)
        assertThat(requirement.featureSpellGrants.single().selectedSpellIds)
            .containsExactly("wizard-cantrip", "cleric-secret")
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should expose Additional Magical Secrets when Lore Bard reaches level six`() {
        // Given
        val lore = Subclass(
            id = "lore",
            name = "Lore",
            desc = emptyList(),
            subclassFlavor = "Bard College",
            levels = listOf(ClassLevel("lore-6", 6, listOf("additional-magical-secrets"))),
        )
        val bard = casterClass(id = "bard", hitDie = 8, subclasses = listOf(lore)) { level ->
            LevelSpellcasting(
                cantrips = 3,
                spells = if (level < 6) 8 else 9,
                spellSlots = mapOf("1" to 4, "3" to 3),
            )
        }
        val spells = listOf(
            spell("ordinary-bard", 3, "bard"),
            spell("druid-secret", 0, "druid"),
            spell("wizard-secret", 3, "wizard"),
        )
        val changes = SpellChanges(
            learned = setOf(ClassSpellRef("bard", "ordinary-bard")),
            featureLearned = mapOf("additional-magical-secrets" to setOf(
                ClassSpellRef("bard", "druid-secret"),
                ClassSpellRef("bard", "wizard-secret"),
            )),
        )

        // When
        val preview = rebuild(
            sheet = CharacterSheet(id = "character", level = 5),
            progression = bardProgression(5, "lore"),
            plan = plan(5, "bard", changes),
            classes = listOf(bard),
            spells = spells,
            features = listOf(Feature("additional-magical-secrets", "Additional Magical Secrets", emptyList())),
        )

        // Then
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().single()
        assertThat(requirement.requiredKnownSpellCount).isEqualTo(1)
        assertThat(requirement.featureSpellGrants.single().featureId).isEqualTo("additional-magical-secrets")
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.SpellPolicy)
    }

    @Test
    fun `rebuild should omit spell decisions when half caster takes first class level`() {
        // Given
        val paladin = casterClass("paladin", 10) { level ->
            if (level == 1) LevelSpellcasting() else LevelSpellcasting(spellSlots = mapOf("1" to 2))
        }
        val ranger = casterClass("ranger", 10) { level ->
            if (level == 1) LevelSpellcasting() else LevelSpellcasting(spells = 2, spellSlots = mapOf("1" to 2))
        }
        val emptyProgression = progression()

        // When
        val paladinRequirements = rebuild(
            CharacterSheet(id = "paladin", level = 0),
            emptyProgression,
            plan(0, "paladin", SpellChanges(), HitPointGain.Fixed(10)),
            listOf(paladin, ranger),
            emptyList(),
        ).requirements
        val rangerRequirements = rebuild(
            CharacterSheet(id = "ranger", level = 0),
            emptyProgression,
            plan(0, "ranger", SpellChanges(), HitPointGain.Fixed(10)),
            listOf(paladin, ranger),
            emptyList(),
        ).requirements

        // Then
        assertThat(paladinRequirements.filterIsInstance<LevelUpRequirement.SpellDecisions>()).isEmpty()
        assertThat(rangerRequirements.filterIsInstance<LevelUpRequirement.SpellDecisions>()).isEmpty()
    }

    @Test
    fun `rebuild should reject spell changes when selected class has no spell policy`() {
        // Given
        val fighter = casterClass("fighter", 10) { null }
        val unsupported = casterClass("artificer", 8) { null }
        val fighterChanges = SpellChanges(learned = setOf(ClassSpellRef("fighter", "borrowed")))
        val unsupportedChanges = SpellChanges(learned = setOf(ClassSpellRef("artificer", "borrowed")))
        val borrowed = spell("borrowed", 1, "fighter", "artificer")

        // When
        val fighterPreview = rebuild(
            CharacterSheet(id = "fighter", level = 0),
            progression(),
            plan(0, "fighter", fighterChanges, HitPointGain.Fixed(10)),
            listOf(fighter, unsupported),
            listOf(borrowed),
        )
        val unsupportedPreview = rebuild(
            CharacterSheet(id = "artificer", level = 0),
            progression(),
            plan(0, "artificer", unsupportedChanges, HitPointGain.Fixed(8)),
            listOf(fighter, unsupported),
            listOf(borrowed),
        )

        // Then
        assertThat(fighterPreview.validations.map { it.code }).contains(LevelUpValidationCode.SpellPolicy)
        assertThat(unsupportedPreview.validations.map { it.code }).contains(LevelUpValidationCode.SpellPolicy)
    }

    private fun rebuild(
        sheet: CharacterSheet,
        progression: CharacterProgression,
        plan: LevelUpPlan,
        classes: List<CharacterClass>,
        spells: List<Spell>,
        features: List<Feature> = emptyList(),
    ) = LevelUpProgressionEngine.rebuild(
        sheet,
        progression,
        plan,
        LevelUpReferenceData(
            classes = classes,
            features = features,
            spells = spells,
            referenceDataVersion = REFERENCE_VERSION,
        ),
    )

    private fun plan(
        expectedLevel: Int,
        classId: String,
        spellChanges: SpellChanges,
        hitPointGain: HitPointGain = HitPointGain.Fixed(5),
    ) = LevelUpPlan(
        expectedTotalLevel = expectedLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = REFERENCE_VERSION,
        selectedClassId = classId,
        selections = LevelUpSelections(hitPointGain = hitPointGain, spellChanges = spellChanges),
    )

    private fun progression(vararg records: CharacterLevelRecord) = CharacterProgression(
        referenceDataVersion = REFERENCE_VERSION,
        origin = ProgressionOrigin.Guided,
        levels = records.toList(),
    )

    private fun bardProgression(levels: Int, subclassId: String? = null) = progression(
        *(1..levels).map { level ->
            record(level, "bard", level).copy(subclassId = subclassId?.takeIf { level >= 3 })
        }.toTypedArray(),
    )

    private fun record(
        characterLevel: Int,
        classId: String,
        classLevel: Int,
        spellChanges: SpellChanges = SpellChanges(),
    ) = CharacterLevelRecord(
        characterLevel = characterLevel,
        classId = classId,
        classLevel = classLevel,
        hitPointGain = HitPointGain.Fixed(if (characterLevel == 1) 8 else 5),
        spellChanges = spellChanges,
    )

    private fun casterClass(
        id: String,
        hitDie: Int,
        levelFeatures: Map<Int, List<String>> = emptyMap(),
        subclasses: List<Subclass> = emptyList(),
        spellcasting: (Int) -> LevelSpellcasting?,
    ) = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        multiClassing = MultiClassing(),
        hitDie = hitDie,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = subclasses,
        levels = (1..20).map { level ->
            ClassLevel("$id-$level", level, levelFeatures[level].orEmpty(), spellcasting(level))
        },
    )

    private fun spell(id: String, level: Int, vararg classIds: String) = Spell(
        id = id,
        name = id.replace('-', ' ').replaceFirstChar { it.uppercase() },
        desc = emptyList(),
        level = level,
        range = "Self",
        ritual = false,
        school = EntityRef("evocation"),
        duration = "Instantaneous",
        castingTime = "1 action",
        classes = classIds.map(::EntityRef),
        components = emptyList(),
        concentration = false,
        source = "SRD",
    )

    private companion object {
        const val REFERENCE_VERSION = "test-v1"
    }
}
