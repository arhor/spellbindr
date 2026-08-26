package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.database.CharacterProgressionJsonCodec
import io.github.arhor.dnd.companion.data.local.database.SpellbindrDatabase
import io.github.arhor.dnd.companion.data.local.database.dao.CharacterDao
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterEntity
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterProgressionEntity
import io.github.arhor.dnd.companion.data.local.database.model.CharacterWithProgressionEntity
import io.github.arhor.dnd.companion.data.mapper.toSnapshot
import io.github.arhor.dnd.companion.domain.model.AbilityIds
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.CharacterCreationResult
import io.github.arhor.dnd.companion.domain.model.CharacterLevelRecord
import io.github.arhor.dnd.companion.domain.model.CharacterProgression
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.CharacterWithProgression
import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.ProgressionOrigin
import io.github.arhor.dnd.companion.domain.model.ProgressionState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class CharacterRepositoryImplTest {

    private val characterDao = mockk<CharacterDao>()
    private val progressionJsonCodec = CharacterProgressionJsonCodec(
        Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    )
    private val database = mockk<SpellbindrDatabase>(relaxed = true)
    private val repository = CharacterRepositoryImpl(characterDao, progressionJsonCodec, database)

    @Test
    fun `observeCharacterWithProgression should map unmanaged state when progression row is missing`() = runTest {
        // Given
        val sheet = testSheet()
        every { characterDao.observeCharacterWithProgression(sheet.id) } returns flowOf(
            CharacterWithProgressionEntity(
                character = CharacterEntity(id = sheet.id, manualSheet = sheet.toSnapshot()),
                progression = null,
            )
        )

        // When
        val result = repository.observeCharacterWithProgression(sheet.id).first()

        // Then
        assertThat(result).isEqualTo(CharacterWithProgression(sheet, ProgressionState.Unmanaged))
    }

    @Test
    fun `observeCharacterWithProgression should map managed state when progression row is present`() = runTest {
        // Given
        val sheet = testSheet()
        val progression = testProgression()
        every { characterDao.observeCharacterWithProgression(sheet.id) } returns flowOf(
            CharacterWithProgressionEntity(
                character = CharacterEntity(id = sheet.id, manualSheet = sheet.toSnapshot()),
                progression = CharacterProgressionEntity(
                    characterId = sheet.id,
                    stateJson = progressionJsonCodec.encode(ProgressionState.Managed(progression)),
                ),
            )
        )

        // When
        val result = repository.observeCharacterWithProgression(sheet.id).first()

        // Then
        assertThat(result).isEqualTo(
            CharacterWithProgression(sheet, ProgressionState.Managed(progression))
        )
    }

    @Test
    fun `saveGuidedCharacter should save sheet and managed progression in one DAO transaction`() = runTest {
        // Given
        val sheet = testSheet()
        val result = CharacterCreationResult(sheet = sheet, progression = testProgression())
        val savedCharacter = slot<CharacterEntity>()
        val savedProgression = slot<CharacterProgressionEntity>()
        coEvery { characterDao.saveCharacterWithProgression(any(), any()) } returns Unit

        // When
        repository.saveGuidedCharacter(result)

        // Then
        coVerify(exactly = 1) {
            characterDao.saveCharacterWithProgression(capture(savedCharacter), capture(savedProgression))
        }
        assertThat(savedCharacter.captured).isEqualTo(
            CharacterEntity(
                id = sheet.id,
                name = sheet.name,
                race = EntityRef(sheet.race),
                background = EntityRef(sheet.background),
                classes = mapOf(EntityRef(sheet.className) to sheet.level),
                abilityScores = mapOf(
                    EntityRef(AbilityIds.STR) to sheet.abilityScores.strength,
                    EntityRef(AbilityIds.DEX) to sheet.abilityScores.dexterity,
                    EntityRef(AbilityIds.CON) to sheet.abilityScores.constitution,
                    EntityRef(AbilityIds.INT) to sheet.abilityScores.intelligence,
                    EntityRef(AbilityIds.WIS) to sheet.abilityScores.wisdom,
                    EntityRef(AbilityIds.CHA) to sheet.abilityScores.charisma,
                ),
                manualSheet = sheet.toSnapshot(),
            )
        )
        assertThat(savedProgression.captured.characterId).isEqualTo(sheet.id)
        assertThat(progressionJsonCodec.decode(savedProgression.captured.stateJson)).isEqualTo(
            ProgressionState.Managed(result.progression)
        )
    }

    private fun testSheet() = CharacterSheet(
        id = "character-1",
        name = "Tamsin",
        className = "wizard",
        race = "human",
        background = "sage",
        abilityScores = AbilityScores(
            strength = 8,
            dexterity = 14,
            constitution = 13,
            intelligence = 16,
            wisdom = 12,
            charisma = 10,
        ),
    )

    private fun testProgression() = CharacterProgression(
        referenceDataVersion = "srd-5e-2014-2026-07-29",
        origin = ProgressionOrigin.Guided,
        levels = listOf(
            CharacterLevelRecord(
                characterLevel = 1,
                classId = "wizard",
                classLevel = 1,
                hitPointGain = HitPointGain.Fixed(6),
            )
        ),
    )
}
