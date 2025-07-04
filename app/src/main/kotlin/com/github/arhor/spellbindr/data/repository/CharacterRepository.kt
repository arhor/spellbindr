package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.datasource.local.db.CharacterDao
import com.github.arhor.spellbindr.data.model.Character
import com.github.arhor.spellbindr.data.model.CharacterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao
) {

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
}
