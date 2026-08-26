package io.github.arhor.dnd.companion.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.arhor.dnd.companion.data.local.database.CharacterProgressionJsonCodec
import io.github.arhor.dnd.companion.data.local.database.SpellbindrDatabase
import io.github.arhor.dnd.companion.data.local.database.converter.CharacterSheetConverter
import io.github.arhor.dnd.companion.data.local.database.converter.EntityRefConverter
import io.github.arhor.dnd.companion.data.local.database.dao.CharacterDao
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterEntity
import io.github.arhor.dnd.companion.data.mapper.toDomain
import io.github.arhor.dnd.companion.data.mapper.toEntity
import io.github.arhor.dnd.companion.data.mapper.toSnapshot
import io.github.arhor.dnd.companion.domain.model.AbilityIds
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.ApplyLevelUpResult
import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.CharacterLevelRecord
import io.github.arhor.dnd.companion.domain.model.CharacterProgression
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.ClassLevel
import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.LevelUpReferenceData
import io.github.arhor.dnd.companion.domain.model.LevelUpSelections
import io.github.arhor.dnd.companion.domain.model.MultiClassing
import io.github.arhor.dnd.companion.domain.model.ProgressionOrigin
import io.github.arhor.dnd.companion.domain.model.ProgressionState
import io.github.arhor.dnd.companion.domain.model.Spellcasting
import io.github.arhor.dnd.companion.domain.model.calculateSpellcastingClassStats
import io.github.arhor.dnd.companion.domain.usecase.LevelUpProgressionEngine
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterLevelUpSpellcastingStatsIntegrationTest {

    private lateinit var database: SpellbindrDatabase
    private lateinit var dao: CharacterDao
    private lateinit var codec: CharacterProgressionJsonCodec
    private lateinit var repository: CharacterRepositoryImpl

    @Before
    fun setUp() {
        val json = Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SpellbindrDatabase::class.java,
        )
            .addTypeConverter(CharacterSheetConverter(json))
            .addTypeConverter(EntityRefConverter(json))
            .allowMainThreadQueries()
            .build()
        dao = database.characterDao()
        codec = CharacterProgressionJsonCodec(json)
        repository = CharacterRepositoryImpl(dao, codec, database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `apply level up should persist single class stats from resulting preview`() = runBlocking {
        val arcanist = casterClass("arcanist", AbilityIds.INT, hitDie = 6)
        val data = LevelUpReferenceData(
            classes = listOf(arcanist),
            features = emptyList(),
            referenceDataVersion = REFERENCE_VERSION,
        )
        val progression = progression("arcanist", "arcanist", "arcanist", "arcanist")
        val sheet = CharacterSheet(
            id = CHARACTER_ID,
            name = "Aster",
            level = 4,
            className = "Arcanist 4",
            abilityScores = AbilityScores(intelligence = 16),
        )
        val plan = plan(expectedLevel = 4, selectedClassId = "arcanist", hitPointGain = 4)
        seed(sheet, progression)
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, data)
        val expectedStats = preview.after.calculateSpellcastingClassStats(data.classes)

        val result = repository.applyLevelUp(CHARACTER_ID, 4, plan, data)
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
            .character.manualSheet?.toDomain(CHARACTER_ID)

        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        requireNotNull(stored)
        assertThat(expectedStats.getValue("arcanist").spellSaveDc).isEqualTo(14)
        assertThat(expectedStats.getValue("arcanist").spellAttackBonus).isEqualTo(6)
        assertThat(stored.managedProgression?.spellcastingClassStats).isEqualTo(expectedStats)
        assertThat((result as ApplyLevelUpResult.Success).sheet.managedProgression?.spellcastingClassStats)
            .isEqualTo(expectedStats)
    }

    @Test
    fun `apply level up should persist separate multiclass spellcasting stats`() = runBlocking {
        val arcanist = casterClass("arcanist", AbilityIds.INT, hitDie = 6)
        val priest = casterClass("priest", AbilityIds.WIS, hitDie = 8)
        val guardian = characterClass("guardian", hitDie = 10)
        val data = LevelUpReferenceData(
            classes = listOf(arcanist, priest, guardian),
            features = emptyList(),
            referenceDataVersion = REFERENCE_VERSION,
        )
        val progression = progression("arcanist", "priest", "guardian", "guardian")
        val sheet = CharacterSheet(
            id = CHARACTER_ID,
            name = "Tamsin",
            level = 4,
            className = "Arcanist 1 / Priest 1 / Guardian 2",
            abilityScores = AbilityScores(intelligence = 16, wisdom = 14),
        )
        val plan = plan(expectedLevel = 4, selectedClassId = "guardian", hitPointGain = 6)
        seed(sheet, progression)
        val preview = LevelUpProgressionEngine.rebuild(sheet, progression, plan, data)
        val expectedStats = preview.after.calculateSpellcastingClassStats(data.classes)

        val result = repository.applyLevelUp(CHARACTER_ID, 4, plan, data)
        val stored = requireNotNull(dao.getCharacterWithProgression(CHARACTER_ID))
            .character.manualSheet?.toDomain(CHARACTER_ID)

        assertThat(result).isInstanceOf(ApplyLevelUpResult.Success::class.java)
        requireNotNull(stored)
        assertThat(expectedStats.keys).containsExactly("arcanist", "priest")
        assertThat(expectedStats.getValue("arcanist").spellSaveDc).isEqualTo(14)
        assertThat(expectedStats.getValue("arcanist").spellAttackBonus).isEqualTo(6)
        assertThat(expectedStats.getValue("priest").spellSaveDc).isEqualTo(13)
        assertThat(expectedStats.getValue("priest").spellAttackBonus).isEqualTo(5)
        assertThat(stored.managedProgression?.spellcastingClassStats).isEqualTo(expectedStats)
    }

    private suspend fun seed(sheet: CharacterSheet, progression: CharacterProgression) {
        dao.saveCharacterWithProgression(
            character = CharacterEntity(
                id = CHARACTER_ID,
                name = sheet.name,
                classes = progression.classLevels.mapKeys { EntityRef(it.key) },
                manualSheet = sheet.toSnapshot(),
            ),
            progression = ProgressionState.Managed(progression).toEntity(CHARACTER_ID, codec),
        )
    }

    private fun plan(
        expectedLevel: Int,
        selectedClassId: String,
        hitPointGain: Int,
    ): LevelUpPlan = LevelUpPlan(
        expectedTotalLevel = expectedLevel,
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = REFERENCE_VERSION,
        selectedClassId = selectedClassId,
        selections = LevelUpSelections(hitPointGain = HitPointGain.Fixed(hitPointGain)),
    )

    private fun progression(vararg classIds: String): CharacterProgression = CharacterProgression(
        referenceDataVersion = REFERENCE_VERSION,
        origin = ProgressionOrigin.Guided,
        levels = classIds.mapIndexed { index, classId ->
            CharacterLevelRecord(
                characterLevel = index + 1,
                classId = classId,
                classLevel = classIds.take(index + 1).count { it == classId },
                hitPointGain = HitPointGain.Fixed(6),
            )
        },
    )

    private fun casterClass(
        id: String,
        abilityId: String,
        hitDie: Int,
    ): CharacterClass = characterClass(id, hitDie).copy(
        spellcasting = Spellcasting(
            info = emptyList(),
            level = 1,
            spellcastingAbility = EntityRef(abilityId),
        ),
    )

    private fun characterClass(id: String, hitDie: Int): CharacterClass = CharacterClass(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        multiClassing = MultiClassing(),
        hitDie = hitDie,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        subclasses = emptyList(),
        levels = (1..20).map { level -> ClassLevel("$id-$level", level, emptyList()) },
    )

    private companion object {
        private const val CHARACTER_ID = "spellcasting-level-up"
        private const val REFERENCE_VERSION = "test-v1"
    }
}
