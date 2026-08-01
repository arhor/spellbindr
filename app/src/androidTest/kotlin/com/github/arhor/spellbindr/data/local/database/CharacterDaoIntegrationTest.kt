package com.github.arhor.spellbindr.data.local.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.arhor.spellbindr.data.local.database.converter.CharacterSheetConverter
import com.github.arhor.spellbindr.data.local.database.converter.EntityRefConverter
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterSheetSnapshot
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDaoIntegrationTest {

    private lateinit var database: SpellbindrDatabase
    private lateinit var dao: CharacterDao
    private lateinit var progressionJsonCodec: CharacterProgressionJsonCodec

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
        progressionJsonCodec = CharacterProgressionJsonCodec(json)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeCharacterWithProgression should return exact relation when transaction commits managed state`() =
        runBlocking {
            // Given
            val character = characterEntity(currentHitPoints = 7)
            val progressionState = managedProgressionState()

            // When
            dao.saveCharacterWithProgression(
                character = character,
                progression = progressionEntity(progressionState),
            )
            val relation = dao.observeCharacterWithProgression(CHARACTER_ID).first()

            // Then
            assertThat(relation?.character).isEqualTo(character)
            assertThat(
                relation?.progression?.stateJson?.let(progressionJsonCodec::decode)
            ).isEqualTo(progressionState)
        }

    @Test
    fun `saveCharacter should preserve managed progression when existing sheet play state changes`() = runBlocking {
        // Given
        val progressionState = managedProgressionState()
        val originalCharacter = characterEntity(currentHitPoints = 7)
        dao.saveCharacterWithProgression(
            character = originalCharacter,
            progression = progressionEntity(progressionState),
        )
        val updatedCharacter = originalCharacter.copy(
            name = "Aster after combat",
            manualSheet = originalCharacter.manualSheet?.copy(
                currentHitPoints = 3,
                temporaryHitPoints = 2,
                notes = "Play-state save",
            ),
        )

        // When
        dao.saveCharacter(updatedCharacter)
        val relation = dao.observeCharacterWithProgression(CHARACTER_ID).first()

        // Then
        assertThat(relation?.character).isEqualTo(updatedCharacter)
        assertThat(
            relation?.progression?.stateJson?.let(progressionJsonCodec::decode)
        ).isEqualTo(progressionState)
    }

    @Test
    fun `saveCharacterWithProgression should roll back parent when child violates foreign key`() = runBlocking {
        // Given
        val character = characterEntity(currentHitPoints = 7)
        val invalidProgression = progressionEntity(
            state = managedProgressionState(),
            characterId = "missing-parent",
        )

        // When
        val failure = runCatching {
            dao.saveCharacterWithProgression(character, invalidProgression)
        }.exceptionOrNull()

        // Then
        assertThat(failure).isNotNull()
        assertThat(dao.getCharacterById(CHARACTER_ID).first()).isNull()
        assertThat(progressionRowCount()).isEqualTo(0)
    }

    @Test
    fun `deleteCharacter should cascade progression when managed parent is deleted`() = runBlocking {
        // Given
        dao.saveCharacterWithProgression(
            character = characterEntity(currentHitPoints = 7),
            progression = progressionEntity(managedProgressionState()),
        )
        assertThat(progressionRowCount()).isEqualTo(1)

        // When
        dao.deleteCharacter(CHARACTER_ID)

        // Then
        assertThat(dao.observeCharacterWithProgression(CHARACTER_ID).first()).isNull()
        assertThat(progressionRowCount()).isEqualTo(0)
    }

    private fun characterEntity(currentHitPoints: Int): CharacterEntity = CharacterEntity(
        id = CHARACTER_ID,
        name = "Aster",
        manualSheet = CharacterSheetSnapshot(
            name = "Aster",
            className = "wizard",
            currentHitPoints = currentHitPoints,
            maxHitPoints = 9,
        ),
    )

    private fun progressionEntity(
        state: ProgressionState,
        characterId: String = CHARACTER_ID,
    ): CharacterProgressionEntity = CharacterProgressionEntity(
        characterId = characterId,
        stateJson = progressionJsonCodec.encode(state),
    )

    private fun managedProgressionState(): ProgressionState = ProgressionState.Managed(
        CharacterProgression(
            referenceDataVersion = "srd-5e-2014-data-v1",
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                CharacterLevelRecord(
                    characterLevel = 1,
                    classId = "wizard",
                    classLevel = 1,
                    hitPointGain = HitPointGain.Fixed(6),
                ),
            ),
        ),
    )

    private fun progressionRowCount(): Int = database.openHelper.readableDatabase
        .query("SELECT COUNT(*) FROM `character_progressions`")
        .use { cursor ->
            check(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private companion object {
        private const val CHARACTER_ID = "character-1"
    }
}
