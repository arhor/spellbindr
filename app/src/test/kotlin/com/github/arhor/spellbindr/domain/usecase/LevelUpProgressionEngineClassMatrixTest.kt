package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceRules
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
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

/** Regression matrix for every bundled class and each of its twenty class levels. */
@RunWith(Parameterized::class)
class LevelUpProgressionEngineClassMatrixTest(private val classId: String) {

    @Test
    fun `rebuild materializes each class milestone without regressing prior features`() {
        val clazz = data.classesById.getValue(classId)
        var progression = emptyProgression()

        (1..20).forEach { classLevel ->
            val sheet = CharacterSheet(id = "matrix-$classId", level = progression.totalLevel)
            val subclassId = clazz.subclasses.firstOrNull()?.id
                ?.takeIf { classLevel == LevelUpReferenceRules.policyFor(classId)?.subclass?.level }
            val selections = LevelUpSelections(
                hitPointGain = HitPointGain.Fixed(if (progression.totalLevel == 0) clazz.hitDie else clazz.hitDie / 2 + 1),
                subclassId = subclassId,
            )
            val plan = LevelUpPlan(
                expectedTotalLevel = progression.totalLevel,
                rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
                referenceDataVersion = data.referenceDataVersion,
                selectedClassId = classId,
                selections = selections,
            )

            val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, data)
            val expectedClassFeatures = clazz.levels.single { it.level == classLevel }.features.toSet()
            val subclass = subclassId ?: progression.levels.lastOrNull()?.subclassId
            val expectedSubclassFeatures = clazz.subclasses.firstOrNull { it.id == subclass }
                ?.levels.orEmpty().firstOrNull { it.level == classLevel }?.features.orEmpty().toSet()
            val gainedFeatures = preview.after.featureIds - preview.before.featureIds
            val expectedFeatureChoiceIds = (expectedClassFeatures + expectedSubclassFeatures)
                .filter { featureId -> data.featuresById[featureId]?.choice != null }
                .map { featureId -> "$featureId:choice" }
                .toSet()

            assertThat(preview.after.totalLevel).isEqualTo(classLevel)
            assertThat(preview.after.classLevels).containsExactly(classId, classLevel)
            assertThat(preview.after.proficiencyBonus).isEqualTo(2 + ((classLevel - 1) / 4))
            assertThat(gainedFeatures).containsExactlyElementsIn(expectedClassFeatures + expectedSubclassFeatures)
            assertThat(preview.after.featureIds).containsAtLeastElementsIn(preview.before.featureIds)
            assertThat(preview.after.featureIds.intersect(preview.before.featureIds))
                .containsExactlyElementsIn(preview.before.featureIds)
            assertThat(preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
                .filter { it.category == com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory.Feature }
                .map { it.id })
                .containsExactlyElementsIn(expectedFeatureChoiceIds)

            val subclassRequirement = preview.requirements.filterIsInstance<LevelUpRequirement.SubclassSelection>()
            val subclassLevel = LevelUpReferenceRules.policyFor(classId)?.subclass?.level
            if (classLevel == subclassLevel) {
                assertThat(subclassRequirement).hasSize(1)
                assertThat(subclassRequirement.single().options.map { it.id })
                    .containsExactlyElementsIn(clazz.subclasses.map { it.id })
            } else {
                assertThat(subclassRequirement).isEmpty()
            }

            val asiRequirements = preview.requirements.filterIsInstance<LevelUpRequirement.AbilityScoreImprovement>()
            if (classLevel in LevelUpReferenceRules.policyFor(classId)!!.abilityScoreImprovement.levels) {
                assertThat(asiRequirements).hasSize(1)
                assertThat(asiRequirements.single().abilityPoints).isEqualTo(2)
            } else {
                assertThat(asiRequirements).isEmpty()
            }

            val proficiencyRequirements = preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
                .filter { it.category.name == "Proficiency" }
            if (classLevel == 1) {
                assertThat(proficiencyRequirements.size).isEqualTo(clazz.multiClassing?.proficiencyChoices?.size ?: 0)
            } else {
                assertThat(proficiencyRequirements).isEmpty()
            }

            progression = progression.copy(levels = progression.levels + CharacterLevelRecord(
                characterLevel = classLevel,
                classId = classId,
                classLevel = classLevel,
                subclassId = subclassId ?: subclass,
                hitPointGain = selections.hitPointGain!!,
                abilityScoreDecision = if (classLevel in LevelUpReferenceRules.policyFor(classId)!!.abilityScoreImprovement.levels) {
                    AbilityScoreDecision.Increase(mapOf("strength" to 2))
                } else null,
            ))
        }
    }

    @Test
    fun `rebuild reports a missing class level with the class name`() {
        val clazz = data.classesById.getValue(classId)
        val broken = data.copy(classes = data.classes.map { candidate ->
            if (candidate.id == classId) candidate.copy(levels = candidate.levels.filterNot { it.level == 20 }) else candidate
        })
        val progression = CharacterProgression(
            referenceDataVersion = broken.referenceDataVersion,
            origin = ProgressionOrigin.Guided,
            levels = (1..19).map { level ->
                CharacterLevelRecord(level, classId, level, hitPointGain = HitPointGain.Fixed(clazz.hitDie))
            },
        )
        val plan = LevelUpPlan(
            expectedTotalLevel = 19,
            rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
            referenceDataVersion = broken.referenceDataVersion,
            selectedClassId = classId,
            selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(clazz.hitDie)),
        )

        val preview = LevelUpProgressionEngine.rebuild(CharacterSheet("broken-$classId", level = 19), progression, plan, broken)

        assertThat(preview.validations.map { it.code }).contains(LevelUpValidationCode.MissingClassLevel)
        assertThat(preview.validations.single { it.code == LevelUpValidationCode.MissingClassLevel }.message)
            .contains(clazz.name)
    }

    private fun emptyProgression() = CharacterProgression(
        referenceDataVersion = data.referenceDataVersion,
        origin = ProgressionOrigin.Guided,
        levels = emptyList(),
    )

    private companion object {
        val data: LevelUpReferenceData by lazy {
            LevelUpReferenceData(
                classes = decodeAsset("classes.json"),
                features = decodeAsset("features.json"),
                feats = decodeAsset("feats.json"),
                referenceDataVersion = LevelUpReferenceRules.referenceDataVersion,
            )
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun classes(): Collection<Array<String>> = data.classes.map { arrayOf(it.id) }

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

