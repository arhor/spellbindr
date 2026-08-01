package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.database.CharacterProgressionJsonCodec
import com.github.arhor.spellbindr.data.local.database.dao.CharacterDao
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity
import com.github.arhor.spellbindr.data.local.database.model.CharacterWithProgressionEntity
import com.github.arhor.spellbindr.data.mapper.toSnapshot
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
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
    private val repository = CharacterRepositoryImpl(characterDao, progressionJsonCodec)

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
