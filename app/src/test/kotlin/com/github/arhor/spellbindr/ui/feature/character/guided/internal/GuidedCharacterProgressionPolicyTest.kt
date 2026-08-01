package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GuidedCharacterProgressionPolicyTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    @Test
    fun `buildGuidedLevelOneSpellChanges should omit prepared spells when class is cleric or druid`() {
        // Given
        val cantrips = linkedSetOf("guidance", "resistance")
        val preparedSpells = linkedSetOf("cure-wounds", "detect-magic")

        // When
        val changesByClass = listOf("cleric", "druid").associateWith { classId ->
            buildGuidedLevelOneSpellChanges(
                classId = classId,
                cantripSpellIds = cantrips,
                levelOneSpellIds = preparedSpells,
            )
        }

        // Then
        assertThat(changesByClass).containsExactly(
            "cleric",
            SpellChanges(
                learned = setOf(
                    ClassSpellRef(classId = "cleric", spellId = "guidance"),
                    ClassSpellRef(classId = "cleric", spellId = "resistance"),
                ),
            ),
            "druid",
            SpellChanges(
                learned = setOf(
                    ClassSpellRef(classId = "druid", spellId = "guidance"),
                    ClassSpellRef(classId = "druid", spellId = "resistance"),
                ),
            ),
        )
    }

    @Test
    fun `buildGuidedLevelOneSpellChanges should separate spellbook spells when class is wizard`() {
        // Given
        val cantrips = linkedSetOf("fire-bolt", "light")
        val spellbookSpells = linkedSetOf("mage-armor", "magic-missile")

        // When
        val changes = buildGuidedLevelOneSpellChanges(
            classId = "wizard",
            cantripSpellIds = cantrips,
            levelOneSpellIds = spellbookSpells,
        )

        // Then
        assertThat(changes).isEqualTo(
            SpellChanges(
                learned = setOf(
                    ClassSpellRef(classId = "wizard", spellId = "fire-bolt"),
                    ClassSpellRef(classId = "wizard", spellId = "light"),
                ),
                addedToSpellbook = setOf(
                    ClassSpellRef(classId = "wizard", spellId = "mage-armor"),
                    ClassSpellRef(classId = "wizard", spellId = "magic-missile"),
                ),
            ),
        )
    }

    @Test
    fun `buildGuidedLevelOneSpellChanges should learn selected spells when class uses spells known`() {
        // Given
        val cantrips = linkedSetOf("fire-bolt", "ray-of-frost")
        val learnedSpells = linkedSetOf("burning-hands", "shield")

        // When
        val changes = buildGuidedLevelOneSpellChanges(
            classId = "sorcerer",
            cantripSpellIds = cantrips,
            levelOneSpellIds = learnedSpells,
        )

        // Then
        assertThat(changes).isEqualTo(
            SpellChanges(
                learned = setOf(
                    ClassSpellRef(classId = "sorcerer", spellId = "fire-bolt"),
                    ClassSpellRef(classId = "sorcerer", spellId = "ray-of-frost"),
                    ClassSpellRef(classId = "sorcerer", spellId = "burning-hands"),
                    ClassSpellRef(classId = "sorcerer", spellId = "shield"),
                ),
            ),
        )
    }

    @Test
    fun `findGuidedLevelOneFeatureChoices should expose bundled subclass choice when draconic is selected`() {
        // Given
        val classes = json.decodeFromString<List<CharacterClass>>(classesAssetPath.toFile().readText())
        val features = json.decodeFromString<List<Feature>>(featuresAssetPath.toFile().readText())
        val sorcerer = classes.single { it.id == "sorcerer" }

        // When
        val choices = findGuidedLevelOneFeatureChoices(
            clazz = sorcerer,
            subclassId = "draconic",
            featuresById = features.associateBy(Feature::id),
        )

        // Then
        assertThat(choices.map { it.first }).containsExactly("dragon-ancestor")
        assertThat(choices.single().second.choose).isEqualTo(1)
    }

    private val classesAssetPath: Path by lazy {
        resolveAssetPath("classes.json")
    }

    private val featuresAssetPath: Path by lazy {
        resolveAssetPath("features.json")
    }

    private fun resolveAssetPath(fileName: String): Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (true) {
            val candidate = current.resolve(Paths.get("app", "src", "main", "assets", "data", fileName))
            if (Files.exists(candidate)) return candidate
            current = current.parent ?: error("Expected bundled asset $fileName")
        }
    }
}
