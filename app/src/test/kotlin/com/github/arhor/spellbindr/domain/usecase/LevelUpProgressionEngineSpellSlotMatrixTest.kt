package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPactMagicCapacity
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@RunWith(Parameterized::class)
class LevelUpProgressionEngineSpellSlotMatrixTest(
    private val displayName: String,
    private val scenario: SpellSlotScenario,
) {

    @Test
    fun `rebuild should derive shared and pact spell capacity when multiclass spellcasting changes`() {
        // Given
        val progression = progressionFor(scenario)
        val preview = LevelUpProgressionEngine.rebuild(
            sheet = qualifiedSheet(progression.totalLevel),
            progression = progression,
            plan = planFor(progression, scenario),
            referenceData = data,
        )

        // When
        val before = preview.before
        val after = preview.after

        // Then
        assertWithMessage("$displayName before shared caster level")
            .that(before.sharedCasterLevel)
            .isEqualTo(scenario.beforeSharedCasterLevel)
        assertWithMessage("$displayName after shared caster level")
            .that(after.sharedCasterLevel)
            .isEqualTo(scenario.afterSharedCasterLevel)
        assertWithMessage("$displayName before shared spell slots")
            .that(before.sharedSpellSlots)
            .isEqualTo(scenario.beforeSharedSpellSlots)
        assertWithMessage("$displayName after shared spell slots")
            .that(after.sharedSpellSlots)
            .isEqualTo(scenario.afterSharedSpellSlots)
        assertPactMagic("before", before.pactMagic, scenario.beforePactMagic)
        assertPactMagic("after", after.pactMagic, scenario.afterPactMagic)
    }

    private fun progressionFor(scenario: SpellSlotScenario): CharacterProgression {
        val classLevels = linkedMapOf<String, Int>()
        return CharacterProgression(
            referenceDataVersion = data.referenceDataVersion,
            origin = ProgressionOrigin.Guided,
            levels = scenario.progressionClassIds.mapIndexed { index, classId ->
                val classLevel = classLevels.getOrDefault(classId, 0) + 1
                classLevels[classId] = classLevel
                CharacterLevelRecord(
                    characterLevel = index + 1,
                    classId = classId,
                    classLevel = classLevel,
                    subclassId = subclassIdFor(classId, classLevel, scenario.progressionSubclassIds),
                    hitPointGain = HitPointGain.Fixed(1),
                )
            },
        )
    }

    private fun planFor(
        progression: CharacterProgression,
        scenario: SpellSlotScenario,
    ): LevelUpPlan = LevelUpPlan(
        expectedTotalLevel = progression.totalLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = data.referenceDataVersion,
        selectedClassId = scenario.selectedClassId,
        selections = LevelUpSelections(
            subclassId = scenario.selectedSubclassId,
            hitPointGain = HitPointGain.Fixed(1),
        ),
    )

    private fun subclassIdFor(
        classId: String,
        classLevel: Int,
        subclassIds: Map<String, String>,
    ): String? {
        val subclassId = subclassIds[classId] ?: return null
        val requiredLevel = LevelUpReferenceRules.policyFor(classId)?.subclass?.level ?: return null
        return subclassId.takeIf { classLevel >= requiredLevel }
    }

    private fun qualifiedSheet(level: Int): CharacterSheet = CharacterSheet(
        id = "spell-slot-matrix-$level",
        level = level,
        abilityScores = AbilityScores(
            strength = 14,
            dexterity = 14,
            constitution = 14,
            intelligence = 14,
            wisdom = 14,
            charisma = 14,
        ),
    )

    private fun assertPactMagic(
        phase: String,
        actual: LevelUpPactMagicCapacity?,
        expected: PactMagicExpectation?,
    ) {
        assertWithMessage("$displayName $phase pact magic")
            .that(actual?.let { PactMagicExpectation(it.slotLevel, it.slots) })
            .isEqualTo(expected)
    }

    data class SpellSlotScenario(
        val displayName: String,
        val progressionClassIds: List<String>,
        val selectedClassId: String,
        val selectedSubclassId: String? = null,
        val progressionSubclassIds: Map<String, String> = emptyMap(),
        val beforeSharedCasterLevel: Int,
        val afterSharedCasterLevel: Int,
        val beforeSharedSpellSlots: Map<Int, Int>,
        val afterSharedSpellSlots: Map<Int, Int>,
        val beforePactMagic: PactMagicExpectation? = null,
        val afterPactMagic: PactMagicExpectation? = null,
    )

    data class PactMagicExpectation(
        val slotLevel: Int,
        val slots: Int,
    )

    private companion object {
        val data: LevelUpReferenceData by lazy {
            LevelUpReferenceData(
                classes = decodeAsset("classes.json"),
                features = emptyList(),
                referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
            )
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun scenarios(): Collection<Array<Any>> = listOf(
            SpellSlotScenario(
                displayName = "wizard 1 -> wizard 2 applies full-caster progression directly",
                progressionClassIds = listOf("wizard"),
                selectedClassId = "wizard",
                beforeSharedCasterLevel = 1,
                afterSharedCasterLevel = 2,
                beforeSharedSpellSlots = mapOf(1 to 2),
                afterSharedSpellSlots = mapOf(1 to 3),
            ),
            SpellSlotScenario(
                displayName = "wizard 1 / ranger 1 -> wizard 1 / ranger 2 adds the first half-caster contribution at level 2",
                progressionClassIds = listOf("wizard", "ranger"),
                selectedClassId = "ranger",
                beforeSharedCasterLevel = 1,
                afterSharedCasterLevel = 2,
                beforeSharedSpellSlots = mapOf(1 to 2),
                afterSharedSpellSlots = mapOf(1 to 3),
            ),
            SpellSlotScenario(
                displayName = "paladin 3 / ranger 3 -> paladin 4 / ranger 3 rounds each half-caster independently",
                progressionClassIds = listOf("paladin", "paladin", "paladin", "ranger", "ranger", "ranger"),
                selectedClassId = "paladin",
                beforeSharedCasterLevel = 2,
                afterSharedCasterLevel = 3,
                beforeSharedSpellSlots = mapOf(1 to 3),
                afterSharedSpellSlots = mapOf(1 to 4, 2 to 2),
            ),
            SpellSlotScenario(
                displayName = "wizard 2 / fighter 2 -> wizard 2 / eldritch knight 3 starts third-caster progression at subclass entry",
                progressionClassIds = listOf("wizard", "wizard", "fighter", "fighter"),
                selectedClassId = "fighter",
                selectedSubclassId = "eldritch-knight",
                beforeSharedCasterLevel = 2,
                afterSharedCasterLevel = 3,
                beforeSharedSpellSlots = mapOf(1 to 3),
                afterSharedSpellSlots = mapOf(1 to 4, 2 to 2),
            ),
            SpellSlotScenario(
                displayName = "wizard 1 / paladin 2 / arcane trickster 5 -> wizard 1 / paladin 2 / arcane trickster 6 combines full, half, and third casters",
                progressionClassIds = listOf("wizard", "paladin", "paladin", "rogue", "rogue", "rogue", "rogue", "rogue"),
                selectedClassId = "rogue",
                progressionSubclassIds = mapOf("rogue" to "arcane-trickster"),
                beforeSharedCasterLevel = 3,
                afterSharedCasterLevel = 4,
                beforeSharedSpellSlots = mapOf(1 to 4, 2 to 2),
                afterSharedSpellSlots = mapOf(1 to 4, 2 to 3),
            ),
            SpellSlotScenario(
                displayName = "wizard 3 / warlock 2 -> wizard 3 / warlock 3 keeps Pact Magic separate from shared slots",
                progressionClassIds = listOf("wizard", "wizard", "wizard", "warlock", "warlock"),
                selectedClassId = "warlock",
                beforeSharedCasterLevel = 3,
                afterSharedCasterLevel = 3,
                beforeSharedSpellSlots = mapOf(1 to 4, 2 to 2),
                afterSharedSpellSlots = mapOf(1 to 4, 2 to 2),
                beforePactMagic = PactMagicExpectation(slotLevel = 1, slots = 2),
                afterPactMagic = PactMagicExpectation(slotLevel = 2, slots = 2),
            ),
            SpellSlotScenario(
                displayName = "wizard 1 / paladin 2 / ranger 2 -> wizard 2 / paladin 2 / ranger 2 recalculates slots when returning to an earlier class",
                progressionClassIds = listOf("wizard", "paladin", "paladin", "ranger", "ranger"),
                selectedClassId = "wizard",
                beforeSharedCasterLevel = 3,
                afterSharedCasterLevel = 4,
                beforeSharedSpellSlots = mapOf(1 to 4, 2 to 2),
                afterSharedSpellSlots = mapOf(1 to 4, 2 to 3),
            ),
        ).map { scenario -> arrayOf(scenario.displayName, scenario) }

        private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

        private inline fun <reified T> decodeAsset(fileName: String): T =
            json.decodeFromString(assetPath(fileName).toFile().readText())

        private fun assetPath(fileName: String): Path {
            var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
            while (true) {
                val candidate = current.resolve(Paths.get("app", "src", "main", "assets", "data", fileName))
                if (Files.exists(candidate)) return candidate
                current = current.parent ?: error("Expected bundled asset $fileName")
            }
        }
    }
}
