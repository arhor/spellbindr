package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.CharacterEntity
import com.github.arhor.spellbindr.data.local.db.CharacterDao
import com.github.arhor.spellbindr.data.model.Character
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.toDomain
import com.github.arhor.spellbindr.data.model.toSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao
) {

    fun observeCharacterSheets(): Flow<List<CharacterSheet>> =
        characterDao.getAllCharacters().map { entities ->
            entities.mapNotNull { entity ->
                entity.manualSheet?.toDomain(entity.id)
            }
        }

    fun observeCharacterSheet(id: String): Flow<CharacterSheet?> =
        characterDao.getCharacterById(id).map { entity ->
            entity?.manualSheet?.toDomain(entity.id)
        }

    suspend fun upsertCharacterSheet(sheet: CharacterSheet) {
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

    fun getCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toCharacter() }
        }
    }

    fun getCharacter(id: String): Flow<Character?> {
        return characterDao.getCharacterById(id).map { it?.toCharacter() }
    }

    suspend fun saveCharacter(character: Character) {
        characterDao.saveCharacter(character.toEntity())
    }

    suspend fun deleteCharacter(id: String) {
        characterDao.deleteCharacter(id)
    }

    private fun CharacterEntity.toCharacter() = Character(
        id = id,
        name = name,
        race = race,
        subrace = subrace,
        classes = classes,
        background = background,
        abilityScores = abilityScores,
        proficiencies = proficiencies,
        equipment = equipment,
        inventory = inventory,
        spells = spells
    )

    private fun Character.toEntity() = CharacterEntity(
        id = id,
        name = name,
        race = race,
        subrace = subrace,
        classes = classes,
        background = background,
        abilityScores = abilityScores,
        proficiencies = proficiencies,
        equipment = equipment,
        inventory = inventory,
        spells = spells
    )

    private fun String.asEntityRef(prefix: String, id: String): EntityRef =
        EntityRef(this.takeIf { it.isNotBlank() } ?: "${prefix}_$id")

    private fun CharacterSheet.toClassLevels(): Map<EntityRef, Int> =
        if (className.isBlank()) emptyMap() else mapOf(
            EntityRef(className.trim()) to level.coerceAtLeast(1)
        )

    private fun CharacterSheet.toAbilityScoreMap(): Map<EntityRef, Int> = buildMap {
        put(EntityRef(Ability.STR.name), abilityScores.strength)
        put(EntityRef(Ability.DEX.name), abilityScores.dexterity)
        put(EntityRef(Ability.CON.name), abilityScores.constitution)
        put(EntityRef(Ability.INT.name), abilityScores.intelligence)
        put(EntityRef(Ability.WIS.name), abilityScores.wisdom)
        put(EntityRef(Ability.CHA.name), abilityScores.charisma)
    }
}
