package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpSelections
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Matrix coverage for every subclass in the bundled 5e reference data. */
@RunWith(Parameterized::class)
class LevelUpProgressionEngineSubclassMatrixTest(
    private val classId: String,
    private val subclassId: String,
) {

    private val referenceData = loadReferenceData()
    private val characterClass: CharacterClass = referenceData.classesById.getValue(classId)
    private val subclass = characterClass.subclasses.single { it.id == subclassId }
    private val acquisitionLevel = requireNotNull(LevelUpReferenceRules.policyFor(classId)?.subclass?.level)

    @Test
    fun `subclass selection is required at its acquisition level`() {
        val progression = progression(acquisitionLevel - 1)
        val missingSelection = rebuild(progression, acquisitionLevel, subclassId = null)
        val selected = rebuild(progression, acquisitionLevel, subclassId)

        assertCodes(missingSelection.validations.map { it.code })
            .contains(LevelUpValidationCode.SubclassRequired)
        assertCodes(selected.validations.map { it.code })
            .doesNotContain(LevelUpValidationCode.SubclassRequired)
    }

    @Test
    fun `subclass cannot be selected before its acquisition level`() {
        if (acquisitionLevel == 1) return

        val targetLevel = acquisitionLevel - 1
        val preview = rebuild(progression(targetLevel - 1), targetLevel, subclassId)

        assertCodes(preview.validations.map { it.code })
            .contains(LevelUpValidationCode.StickySubclass)
    }

    @Test
    fun `subclass stays sticky and grants each milestone exactly once`() {
        val subclassMilestones = subclass.levels.orEmpty()
            .map { it.level to it.features.toSet() }
            .filter { (level, _) -> level >= acquisitionLevel }
            .sortedBy { (level, _) -> level }
        var expectedSubclassFeatures = emptySet<String>()

        subclassMilestones.forEach { (level, milestoneFeatures) ->
            val previous = progression(level - 1, subclassId)
            val preview = rebuild(
                progression = previous,
                targetLevel = level,
                subclassId = if (level == acquisitionLevel) subclassId else null,
            )
            expectedSubclassFeatures = expectedSubclassFeatures + milestoneFeatures

            assertFeatures(preview.after.featureIds.filter { it in expectedSubclassFeatures }.toSet())
                .containsExactlyElementsIn(expectedSubclassFeatures)
            assertFeatures(preview.after.featureIds - preview.before.featureIds)
                .containsAtLeastElementsIn(milestoneFeatures)
        }
    }

    private fun rebuild(
        progression: CharacterProgression,
        targetLevel: Int,
        subclassId: String?,
    ) = LevelUpProgressionEngine.rebuild(
        sheet = CharacterSheet(id = "matrix-$classId-$this", level = progression.totalLevel),
        progression = progression,
        plan = LevelUpPlan(
            expectedTotalLevel = progression.totalLevel,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = referenceData.referenceDataVersion,
            selectedClassId = classId,
            selections = LevelUpSelections(
                subclassId = subclassId,
                hitPointGain = HitPointGain.Fixed(hitPointGain(targetLevel)),
            ),
        ),
        referenceData = referenceData,
    )

    private fun progression(classLevel: Int, subclassId: String? = null): CharacterProgression =
        CharacterProgression(
            referenceDataVersion = referenceData.referenceDataVersion,
            origin = ProgressionOrigin.Guided,
            levels = (1..classLevel).map { level ->
                CharacterLevelRecord(
                    characterLevel = level,
                    classId = classId,
                    classLevel = level,
                    subclassId = if (subclassId != null && level >= acquisitionLevel) subclassId else null,
                    hitPointGain = HitPointGain.Fixed(hitPointGain(level)),
                )
            },
        )

    private fun hitPointGain(level: Int): Int = if (level == 1) characterClass.hitDie else characterClass.hitDie / 2 + 1

    private fun assertCodes(actual: List<LevelUpValidationCode>) =
        com.google.common.truth.Truth.assertWithMessage("$classId/$subclassId at subclass level $acquisitionLevel")
            .that(actual)

    private fun assertFeatures(actual: Set<String>) =
        com.google.common.truth.Truth.assertWithMessage("$classId/$subclassId at subclass level $acquisitionLevel")
            .that(actual)

    private companion object {
        private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}/{1}")
        fun subclasses(): Collection<Array<String>> {
            val classes = decode<List<CharacterClass>>("classes.json")
            return classes.flatMap { characterClass ->
                characterClass.subclasses.map { subclass -> arrayOf(characterClass.id, subclass.id) }
            }
        }

        private fun loadReferenceData(): LevelUpReferenceData = LevelUpReferenceData(
            classes = decode("classes.json"),
            features = decode("features.json"),
            feats = decode("feats.json"),
            spells = decode("spells.json"),
            referenceDataVersion = CharacterProgression.BUNDLED_REFERENCE_DATA_VERSION,
        )

        private inline fun <reified T> decode(fileName: String): T =
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
