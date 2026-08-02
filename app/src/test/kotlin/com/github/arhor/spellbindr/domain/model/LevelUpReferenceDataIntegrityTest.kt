package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LevelUpReferenceDataIntegrityTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    @Test
    fun `decode should provide one complete policy for every bundled character class`() {
        // Given
        val classes = decodeAsset<List<CharacterClass>>("classes.json")

        // When
        val policies = classes.associate { it.id to LevelUpReferenceRules.policyFor(it.id) }

        // Then
        assertThat(classes).hasSize(12)
        assertThat(policies.keys).containsExactlyElementsIn(LevelUpReferenceRules.classPolicies.keys)
        assertThat(policies.values).doesNotContain(null)
        assertThat(classes.map { it.multiClassing }).doesNotContain(null)
    }

    @Test
    fun `decode should provide contiguous levels and valid feature references for every class`() {
        // Given
        val classes = decodeAsset<List<CharacterClass>>("classes.json")
        val features = decodeAsset<List<Feature>>("features.json").associateBy(Feature::id)

        // When
        val missingFeatureReferences = classes.flatMap { characterClass ->
            (characterClass.levels + characterClass.subclasses.flatMap { it.levels.orEmpty() })
                .flatMap { level -> level.features.filterNot(features::containsKey) }
                .map { featureId -> "${characterClass.id}:$featureId" }
        }

        // Then
        classes.forEach { characterClass ->
            assertThat(characterClass.levels.map(ClassLevel::level)).containsExactlyElementsIn(1..20).inOrder()
            assertThat(characterClass.levels.map(ClassLevel::id).toSet()).hasSize(20)
        }
        assertThat(missingFeatureReferences).isEmpty()
    }

    @Test
    fun `decode should align every subclass acquisition policy with bundled subclass levels`() {
        // Given
        val classes = decodeAsset<List<CharacterClass>>("classes.json")

        // When
        val acquisitionLevels = classes.associate { characterClass ->
            characterClass.id to characterClass.subclasses.minOf { it.levels.orEmpty().minOf(ClassLevel::level) }
        }

        // Then
        acquisitionLevels.forEach { (classId, level) ->
            assertThat(LevelUpReferenceRules.policyFor(classId)?.subclass?.level).isEqualTo(level)
        }
    }

    @Test
    fun `decode should preserve all typed multiclass prerequisites grants and choices`() {
        // Given
        val classes = decodeAsset<List<CharacterClass>>("classes.json")

        // When
        val multiclassRules = classes.associate { it.id to requireNotNull(it.multiClassing) }

        // Then
        multiclassRules.forEach { (classId, rule) ->
            assertThat(rule.prerequisites).isNotEmpty()
            rule.prerequisites.forEach { prerequisite ->
                assertThat(prerequisite.abilityScore).isNotEmpty()
                assertThat(prerequisite.abilityScore.all { it in AbilityIds.standardOrder }).isTrue()
                assertThat(prerequisite.minimumScore).isAtLeast(1)
            }
            assertThat(rule.proficiencyChoices.mapIndexed { index, _ -> rule.proficiencyChoiceId(classId, index) })
                .containsNoDuplicates()
        }
    }

    @Test
    fun `levelUpCapability should resolve every feature choice to a stable selection requirement`() {
        // Given
        val features = decodeAsset<List<Feature>>("features.json")

        // When
        val capabilities = features.associate { it.id to it.levelUpCapability() }

        // Then
        features.filter { it.choice != null }.forEach { feature ->
            val capability = capabilities.getValue(feature.id)
            assertThat(capability).isInstanceOf(FeatureCapability.Selection::class.java)
            assertThat((capability as FeatureCapability.Selection).choiceId).isEqualTo("${feature.id}:choice")
        }
        assertThat(features.filter { it.choice == null }.map { capabilities.getValue(it.id) }.toSet())
            .containsExactly(FeatureCapability.Descriptive)
    }

    @Test
    fun `decode should provide valid prerequisites effects and choices when every feat is decoded`() {
        // Given
        val feats = decodeAsset<List<Feat>>("feats.json")
        val proficiencyIds = decodeAsset<List<Proficiency>>("proficiencies.json").map(Proficiency::id).toSet()

        // When
        val featChoiceIds = feats.flatMap { it.ownedChoiceIds }

        // Then
        assertThat(feats.map(Feat::id)).containsNoDuplicates()
        assertThat(featChoiceIds).containsNoDuplicates()
        feats.forEach { feat ->
            feat.prerequisites.forEach { prerequisite ->
                when (prerequisite) {
                    is Prerequisite.AbilityScorePrerequisite -> {
                        assertThat(prerequisite.abilityScore.all { it in AbilityIds.standardOrder }).isTrue()
                    }
                    is Prerequisite.ProficiencyPrerequisite -> {
                        assertThat(proficiencyIds).contains(prerequisite.id)
                    }
                    Prerequisite.SpellcastingPrerequisite -> Unit
                    else -> error("Unexpected feat prerequisite for ${feat.id}: $prerequisite")
                }
            }
            feat.abilityBonusChoice?.from?.flatMap { it.keys }?.forEach { abilityId ->
                assertThat(AbilityIds.standardOrder).contains(abilityId)
            }
            feat.languageChoice?.let { choice ->
                assertThat(choice.from).isEqualTo("languages")
                assertThat(choice.choose).isGreaterThan(0)
            }
            feat.proficiencyChoice?.let { choice ->
                assertThat(choice.choose).isGreaterThan(0)
                assertThat(choice.from).containsNoDuplicates()
            }
            feat.damageTypeChoice?.let { choice ->
                assertThat(choice.choose).isGreaterThan(0)
                assertThat(choice.from).containsNoDuplicates()
            }
            if (feat.correlatesAbilityAndSavingThrow) {
                assertThat(feat.abilityBonusChoice).isNotNull()
                assertThat(feat.proficiencyChoice).isNotNull()
                assertThat(feat.correlatedAbilitySavingThrowChoiceId).isNotNull()
            }
        }
        val elementalAdept = feats.single { it.id == "elemental-adept" }
        assertThat(elementalAdept.repeatable).isTrue()
        assertThat(elementalAdept.damageTypeChoice?.from)
            .containsExactly("acid", "cold", "fire", "lightning", "thunder")
        assertThat(elementalAdept.prerequisites).contains(Prerequisite.SpellcastingPrerequisite)
        assertThat(feats.single { it.id == "resilient" }.correlatesAbilityAndSavingThrow).isTrue()
        assertThat(feats.single { it.id == "spell-sniper" }.prerequisites)
            .contains(Prerequisite.SpellcastingPrerequisite)
        assertThat(feats.single { it.id == "war-caster" }.prerequisites)
            .contains(Prerequisite.SpellcastingPrerequisite)
    }

    @Test
    fun `reference rules should provide complete xp and slot tables and spell policies`() {
        // Given
        val classes = decodeAsset<List<CharacterClass>>("classes.json")

        // When
        val policies = classes.associateBy({ it.id }, { LevelUpReferenceRules.policyFor(it.id)!! })

        // Then
        assertThat(LevelUpReferenceRules.experienceThresholds.keys).containsExactlyElementsIn(1..20).inOrder()
        assertThat(LevelUpReferenceRules.sharedSpellSlots.keys).containsExactlyElementsIn(1..20).inOrder()
        LevelUpReferenceRules.sharedSpellSlots.values.forEach { slots ->
            assertThat(slots.keys.all { it in 1..9 }).isTrue()
            assertThat(slots.values.all { it > 0 }).isTrue()
        }
        assertThat(policies.filterValues { it.casterContribution == CasterContribution.Full }.keys)
            .containsExactly("bard", "cleric", "druid", "sorcerer", "wizard")
        assertThat(policies.filterValues { it.casterContribution == CasterContribution.Half }.keys)
            .containsExactly("paladin", "ranger")
        assertThat(policies.getValue("warlock").casterContribution).isEqualTo(CasterContribution.Pact)
        assertThat(LevelUpReferenceRules.pactMagic.classId).isEqualTo("warlock")
    }

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
