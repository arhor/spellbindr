package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.CharacterEntity
import com.github.arhor.spellbindr.data.local.db.CharacterDao
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.data.mapper.toEntity
import com.github.arhor.spellbindr.data.mapper.toSnapshot
import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : CharacterRepository {

    override fun observeCharacterSheets(): Flow<List<CharacterSheet>> =
        characterDao.getAllCharacters().map { entities ->
            entities.mapNotNull { entity ->
                entity.manualSheet?.toDomain(entity.id)
            }
        }

    override fun observeCharacterSheet(id: String): Flow<CharacterSheet?> =
        characterDao.getCharacterById(id).map { entity ->
            entity?.manualSheet?.toDomain(entity.id)
        }

    override suspend fun upsertCharacterSheet(sheet: CharacterSheet) {
        val existing = characterDao.getCharacterById(sheet.id).firstOrNull()
        val base = existing ?: CharacterEntity(id = sheet.id)
        val updated = base.copy(
            id = sheet.id,
            name = sheet.name,
            race = sheet.race.asEntityRef("race", sheet.id),
            background = sheet.background.asEntityRef("background", sheet.id),
            classes = sheet.toClassLevels(),
            abilityScores = sheet.toAbilityScoreMap(),
            manualSheet = sheet.toSnapshot(),
        )
        characterDao.saveCharacter(updated)
    }

    override fun getCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCharacter(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toDomain() }
    }

    override suspend fun saveCharacter(character: Character) {
        characterDao.saveCharacter(character.toEntity())
    }

    override suspend fun deleteCharacter(id: String) {
        characterDao.deleteCharacter(id)
    }

    private fun String.asEntityRef(prefix: String, id: String): EntityRef =
        EntityRef(this.takeIf { it.isNotBlank() } ?: "${prefix}_$id")

    private fun CharacterSheet.toClassLevels(): Map<EntityRef, Int> =
        if (className.isBlank()) emptyMap() else mapOf(
            EntityRef(className.trim()) to level.coerceAtLeast(1)
        )

    private fun CharacterSheet.toAbilityScoreMap(): Map<EntityRef, Int> = buildMap {
        put(Ability.STR.ref, abilityScores.strength)
        put(Ability.DEX.ref, abilityScores.dexterity)
        put(Ability.CON.ref, abilityScores.constitution)
        put(Ability.INT.ref, abilityScores.intelligence)
        put(Ability.WIS.ref, abilityScores.wisdom)
        put(Ability.CHA.ref, abilityScores.charisma)
    }
}
