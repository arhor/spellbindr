package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.AbilityScorePrerequisite
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpDeferredFeatDecision
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
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
import com.github.arhor.spellbindr.domain.model.Effect
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LevelUpProgressionEngineTest {

    @Test
    fun `rebuild should accept the fixed hit point gain when fixed value matches the class rule`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Fixed(6))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should reject the fixed hit point gain when fixed value differs from the class rule`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Fixed(5))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should accept the rolled hit point gain when roll is within the hit die`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Rolled(10))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should accept the manual hit point gain when value is positive`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Manual(25))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should reject the rolled hit point gain when roll exceeds the hit die`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Rolled(11))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should reject the manual hit point gain when value is not positive`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1)
        val progression = progression("fighter")
        val plan = plan(1, "fighter", HitPointGain.Manual(0))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should expose normal fixed gain when taking first level in a later class`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 1, abilityScores = AbilityScores(intelligence = 13))
        val progression = progression("fighter")
        val plan = plan(1, "wizard", HitPointGain.Fixed(4))

        // When
        val requirement = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())
            .requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.HitPoints>()
            .single()

        // Then
        assertThat(requirement.fixedGain).isEqualTo(4)
    }

    @Test
    fun `rebuild should expose full hit die fixed gain when taking first character level`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 0)
        val progression = CharacterProgression(
            referenceDataVersion = "test-v1",
            origin = ProgressionOrigin.Guided,
            levels = emptyList(),
        )
        val plan = plan(0, "fighter", HitPointGain.Fixed(10))

        // When
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, referenceData())

        // Then
        val requirement = preview.requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.HitPoints>()
            .single()
        assertThat(requirement.fixedGain).isEqualTo(10)
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidHitPointGain)
    }

    @Test
    fun `rebuild should accept split ability increases when two scores gain one point`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 3)
        val progression = progression(List(3) { "fighter" })
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1)),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            sheet,
            progression,
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            referenceData(),
        )

        // Then
        assertThat(preview.validations.map { it.code })
            .doesNotContain(LevelUpValidationCode.InvalidAbilityScoreIncrease)
        assertThat(preview.after.abilityScores.strength).isEqualTo(11)
        assertThat(preview.after.abilityScores.dexterity).isEqualTo(11)
    }

    @Test
    fun `rebuild should reject ability increase when an allocation exceeds the score cap`() {
        // Given
        val sheet = CharacterSheet(id = "character", level = 3, abilityScores = AbilityScores(strength = 19))
        val progression = progression(List(3) { "fighter" })
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Increase(mapOf(AbilityIds.STR to 2)),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            sheet,
            progression,
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            referenceData(),
        )

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidAbilityScoreIncrease)
    }

    @Test
    fun `rebuild should reject split ability increase when one selected score is capped`() {
        // Given
        val sheet = CharacterSheet(
            id = "character",
            level = 3,
            abilityScores = AbilityScores(strength = 20, dexterity = 19),
        )
        val progression = progression(List(3) { "fighter" })
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Increase(
                mapOf(AbilityIds.STR to 1, AbilityIds.DEX to 1),
            ),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            sheet,
            progression,
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            referenceData(),
        )

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidAbilityScoreIncrease)
    }

    @Test
    fun `rebuild should expose eligible feats and cap safe feat choices when prerequisites are met`() {
        // Given
        val athlete = Feat(
            id = "athlete",
            name = "Athlete",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.AbilityScorePrerequisite(
                abilityScore = listOf(AbilityIds.STR, AbilityIds.DEX),
                minimumValue = 13,
                atLeastOne = true,
            )),
            abilityBonusChoice = Choice.AbilityBonusChoice(
                choose = 1,
                from = listOf(mapOf(AbilityIds.STR to 1), mapOf(AbilityIds.DEX to 1)),
            ),
        )
        val unavailable = Feat(
            id = "actor",
            name = "Actor",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.AbilityScorePrerequisite(listOf(AbilityIds.CHA), 13)),
        )
        val sheet = CharacterSheet(
            id = "character",
            level = 3,
            abilityScores = AbilityScores(strength = 20, dexterity = 18, charisma = 10),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(athlete.id),
        )
        val data = referenceData().copy(feats = listOf(athlete, unavailable))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            sheet,
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        val asi = preview.requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.AbilityScoreImprovement>()
            .single()
        val featChoice = preview.requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.ChoiceSelection>()
            .single { it.category == com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory.Feat }
        assertThat(asi.eligibleFeatIds).containsExactly(athlete.id)
        assertThat((featChoice.choice as Choice.AbilityBonusChoice).from)
            .containsExactly(mapOf(AbilityIds.DEX to 1))
    }

    @Test
    fun `rebuild should require and materialize language choices when feat owns language selection`() {
        // Given
        val linguist = Feat(
            id = "linguist",
            name = "Linguist",
            desc = emptyList(),
            languageChoice = Choice.ResourceListChoice(choose = 3, from = "languages"),
        )
        val languages = listOf("common", "dwarvish", "elvish").map(::language)
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(linguist.id),
            featChoices = mapOf(linguist.languageChoiceId!! to languages.mapTo(linkedSetOf()) { it.id }),
        )
        val data = referenceData().copy(feats = listOf(linguist), languages = languages)

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        val requirement = preview.requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.ChoiceSelection>()
            .single { it.id == linguist.languageChoiceId }
        assertThat(requirement.options.map { it.id }).containsExactlyElementsIn(languages.map { it.id })
        assertThat(preview.after.languageIds).containsExactlyElementsIn(languages.map { it.id })
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.ChoiceRequired)
    }

    @Test
    fun `rebuild should require and materialize proficiency choices when feat owns proficiency selection`() {
        // Given
        val skilled = Feat(
            id = "skilled",
            name = "Skilled",
            desc = emptyList(),
            proficiencyChoice = Choice.OptionsArrayChoice(
                choose = 3,
                from = listOf("skill-arcana", "skill-history", "thieves-tools"),
            ),
        )
        val selected = skilled.proficiencyChoice!!.from.toSet()
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(skilled.id),
            featChoices = mapOf(skilled.proficiencyChoiceId!! to selected),
        )
        val data = referenceData().copy(feats = listOf(skilled))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        assertThat(preview.after.proficiencyIds).containsAtLeastElementsIn(selected)
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.ChoiceRequired)
    }

    @Test
    fun `rebuild should exclude and reject feat when non repeatable feat was selected previously`() {
        // Given
        val alert = Feat(id = "alert", name = "Alert", desc = emptyList())
        val base = progression(List(3) { "fighter" })
        val existing = base.copy(
            levels = base.levels.mapIndexed { index, record ->
                if (index == 0) record.copy(abilityScoreDecision = AbilityScoreDecision.Feat(alert.id)) else record
            },
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(alert.id),
        )
        val data = referenceData().copy(feats = listOf(alert))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            existing,
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        val asi = preview.requirements
            .filterIsInstance<com.github.arhor.spellbindr.domain.model.LevelUpRequirement.AbilityScoreImprovement>()
            .single()
        assertThat(asi.eligibleFeatIds).doesNotContain(alert.id)
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.FeatAlreadySelected)
    }

    @Test
    fun `rebuild should materialize feat hp and proficiency effects when feat is selected`() {
        // Given
        val feat = Feat(
            id = "durable-training",
            name = "Durable Training",
            desc = emptyList(),
            effects = listOf(
                Effect.AddHpEffect(value = 2, perLevel = true),
                Effect.AddProficienciesEffect(setOf("light-armor")),
            ),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(feat.id),
        )
        val data = referenceData().copy(feats = listOf(feat))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        assertThat(preview.after.maximumHitPoints).isEqualTo(36)
        assertThat(preview.after.proficiencyIds).contains("light-armor")
    }

    @Test
    fun `rebuild should apply one correlated ability and saving throw when resilient is selected`() {
        // Given
        val resilient = resilientFeat()
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(resilient.id),
            featChoices = mapOf(
                resilient.correlatedAbilitySavingThrowChoiceId!! to setOf(AbilityIds.DEX),
            ),
        )
        val data = referenceData().copy(feats = listOf(resilient))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )

        // Then
        assertThat(preview.after.abilityScores.dexterity).isEqualTo(11)
        assertThat(preview.after.savingThrowAbilityIds).contains(AbilityIds.DEX)
        assertThat(preview.after.proficiencyIds).doesNotContain("saving-throw-dex")
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidChoice)
    }

    @Test
    fun `rebuild should reject mismatched legacy choices when resilient selections are split`() {
        // Given
        val resilient = resilientFeat()
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(resilient.id),
            featChoices = mapOf(
                resilient.abilityBonusChoiceId!! to setOf(AbilityIds.STR),
                resilient.proficiencyChoiceId!! to setOf("saving-throw-dex"),
            ),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            referenceData().copy(feats = listOf(resilient)),
        )

        // Then
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidChoice)
        assertThat(preview.after.savingThrowAbilityIds).doesNotContain(AbilityIds.DEX)
    }

    @Test
    fun `rebuild should require a distinct damage type when elemental adept is selected repeatedly`() {
        // Given
        val elementalAdept = Feat(
            id = "elemental-adept",
            name = "Elemental Adept",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.SpellcastingPrerequisite),
            damageTypeChoice = Choice.OptionsArrayChoice(1, listOf("acid", "cold", "fire")),
            repeatable = true,
        )
        val base = progression(List(3) { "wizard" })
        val existing = base.copy(levels = base.levels.mapIndexed { index, record ->
            if (index == 0) record.copy(
                abilityScoreDecision = AbilityScoreDecision.Feat(elementalAdept.id),
                featChoices = mapOf(elementalAdept.damageTypeChoiceId!! to setOf("fire")),
            ) else record
        })
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(4),
            abilityScoreDecision = AbilityScoreDecision.Feat(elementalAdept.id),
            featChoices = mapOf(elementalAdept.damageTypeChoiceId!! to setOf("cold")),
        )
        val data = referenceData().copy(feats = listOf(elementalAdept))

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            existing,
            plan(3, "wizard", HitPointGain.Fixed(4), selections),
            data,
        )
        val record = LevelUpProgressionEngine.recordFor(
            plan(3, "wizard", HitPointGain.Fixed(4), selections),
            wizard,
            4,
            existing,
            data,
            preview.validations,
        )

        // Then
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
            .single { it.id == elementalAdept.damageTypeChoiceId }
        assertThat(requirement.options.map { it.id }).containsExactly("acid", "cold")
        assertThat(preview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.InvalidChoice)
        assertThat(record.featChoices).containsEntry(elementalAdept.damageTypeChoiceId!!, setOf("cold"))
    }

    @Test
    fun `rebuild should expose typed deferral when a feat needs unsupported spell or maneuver choices`() {
        // Given
        val deferredFeats = listOf(
            Feat("magic-initiate", "Magic Initiate", emptyList()),
            Feat("ritual-caster", "Ritual Caster", emptyList()),
            Feat("spell-sniper", "Spell Sniper", emptyList(), listOf(Prerequisite.SpellcastingPrerequisite)),
            Feat("martial-adept", "Martial Adept", emptyList()),
        )
        val selected = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(4),
            abilityScoreDecision = AbilityScoreDecision.Feat("magic-initiate"),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3),
            progression(List(3) { "wizard" }),
            plan(3, "wizard", HitPointGain.Fixed(4), selected),
            referenceData().copy(feats = deferredFeats),
        )

        // Then
        val eligibility = preview.requirements.filterIsInstance<LevelUpRequirement.AbilityScoreImprovement>()
            .single().featEligibility.associateBy { it.featId }
        assertThat(eligibility.getValue("magic-initiate").deferredDecision)
            .isEqualTo(LevelUpDeferredFeatDecision.SpellSelection)
        assertThat(eligibility.getValue("ritual-caster").deferredDecision)
            .isEqualTo(LevelUpDeferredFeatDecision.SpellSelection)
        assertThat(eligibility.getValue("spell-sniper").deferredDecision)
            .isEqualTo(LevelUpDeferredFeatDecision.SpellSelection)
        assertThat(eligibility.getValue("martial-adept").deferredDecision)
            .isEqualTo(LevelUpDeferredFeatDecision.ManeuverSelection)
        assertThat(eligibility.values.map { it.eligible }).containsExactly(false, false, false, false)
        assertThat(eligibility.getValue("magic-initiate").reasons)
            .contains("This feat's spell ownership and casting rules cannot be represented by the bundled progression model.")
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.UnsupportedFeatDecision)
    }

    @Test
    fun `rebuild should accept sheet or class spellcasting when a spellcasting feat is considered`() {
        // Given
        val warCaster = Feat(
            id = "war-caster",
            name = "War Caster",
            desc = emptyList(),
            prerequisites = listOf(Prerequisite.SpellcastingPrerequisite),
        )
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(warCaster.id),
        )
        val data = referenceData().copy(feats = listOf(warCaster))

        // When
        val martialPreview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "martial", level = 3),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )
        val racialCasterPreview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(
                id = "racial-caster",
                level = 3,
                characterSpells = listOf(CharacterSpell("hellish-rebuke", "Tiefling")),
            ),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            data,
        )
        val casterPreview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "caster", level = 3),
            progression(List(3) { "wizard" }),
            plan(3, "wizard", HitPointGain.Fixed(4), selections.copy(hitPointGain = HitPointGain.Fixed(4))),
            data,
        )

        // Then
        assertThat(martialPreview.validations.map { it.code }).contains(LevelUpValidationCode.FeatPrerequisite)
        assertThat(racialCasterPreview.validations.map { it.code })
            .doesNotContain(LevelUpValidationCode.FeatPrerequisite)
        assertThat(casterPreview.validations.map { it.code }).doesNotContain(LevelUpValidationCode.FeatPrerequisite)
    }

    @Test
    fun `rebuild should exclude manual sheet languages when a feat offers new managed languages`() {
        // Given
        val linguist = Feat(
            id = "linguist",
            name = "Linguist",
            desc = emptyList(),
            languageChoice = Choice.ResourceListChoice(choose = 3, from = "languages"),
        )
        val languages = listOf("common", "dwarvish", "elvish", "gnomish", "orc").map(::language)
        val selections = LevelUpSelections(
            hitPointGain = HitPointGain.Fixed(6),
            abilityScoreDecision = AbilityScoreDecision.Feat(linguist.id),
            featChoices = mapOf(linguist.languageChoiceId!! to setOf("common", "dwarvish", "gnomish")),
        )

        // When
        val preview = LevelUpProgressionEngine.rebuild(
            CharacterSheet(id = "character", level = 3, languages = "Common, Elvish"),
            progression(List(3) { "fighter" }),
            plan(3, "fighter", HitPointGain.Fixed(6), selections),
            referenceData().copy(feats = listOf(linguist), languages = languages),
        )

        // Then
        val requirement = preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
            .single { it.id == linguist.languageChoiceId }
        assertThat(requirement.options.map { it.id }).containsExactly("dwarvish", "gnomish", "orc")
        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.InvalidChoice)
        assertThat(preview.before.languageIds).isEmpty()
    }

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
    fun `rebuild should block subclass selection when subclass is selected before its acquisition level`() {
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

    private fun language(id: String) = Language(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        type = "Standard",
        typicalSpeakers = emptyList(),
    )

    private fun resilientFeat() = Feat(
        id = "resilient",
        name = "Resilient",
        desc = emptyList(),
        abilityBonusChoice = Choice.AbilityBonusChoice(
            choose = 1,
            from = listOf(mapOf(AbilityIds.STR to 1), mapOf(AbilityIds.DEX to 1)),
        ),
        proficiencyChoice = Choice.OptionsArrayChoice(
            choose = 1,
            from = listOf("saving-throw-str", "saving-throw-dex"),
        ),
        correlatesAbilityAndSavingThrow = true,
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
