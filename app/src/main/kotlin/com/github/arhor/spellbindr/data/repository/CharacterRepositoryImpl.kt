package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.db.CharacterDao
import com.github.arhor.spellbindr.data.local.db.CharacterEntity
import com.github.arhor.spellbindr.data.local.db.toDomain
import com.github.arhor.spellbindr.data.local.db.toSnapshot
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.data.mapper.toEntity
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [CharacterRepository] backed by a Room database.
 *
 * This repository manages the bidirectional mapping between:
 * - [CharacterSheet] (User inputs from the editor)
 * - [CharacterEntity] (Persistence model)
 * - [Character] (Domain model for gameplay logic)
 */
@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao
) : CharacterRepository {

    /**
     * Observes all characters, converting the persisted snapshots back into [CharacterSheet] objects.
     */
    override fun observeCharacterSheets(): Flow<List<CharacterSheet>> =
        characterDao.getAllCharacters().map { entities ->
            entities.mapNotNull { entity ->
                entity.manualSheet?.toDomain(entity.id)
            }
        }

    /**
     * Observes a single character sheet by ID.
     */
    override fun observeCharacterSheet(id: String): Flow<CharacterSheet?> =
        characterDao.getCharacterById(id).map { entity ->
            entity?.manualSheet?.toDomain(entity.id)
        }

    /**
     * Saves a character sheet.
     * This method fetches the existing entity (if any) to preserve fields not present in the sheet
     * (though currently, the sheet drives most of the entity state via mapping).
     */
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
        put(EntityRef(AbilityIds.STR), abilityScores.strength)
        put(EntityRef(AbilityIds.DEX), abilityScores.dexterity)
        put(EntityRef(AbilityIds.CON), abilityScores.constitution)
        put(EntityRef(AbilityIds.INT), abilityScores.intelligence)
        put(EntityRef(AbilityIds.WIS), abilityScores.wisdom)
        put(EntityRef(AbilityIds.CHA), abilityScores.charisma)
    }
}
