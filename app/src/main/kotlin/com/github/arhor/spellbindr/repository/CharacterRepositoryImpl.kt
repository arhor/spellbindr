package com.github.arhor.spellbindr.repository

import com.github.arhor.spellbindr.data.dao.CharacterDao
import com.github.arhor.spellbindr.data.model.Character
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val dao: CharacterDao,
) : CharacterRepository {

    override fun getCharacters(): Flow<List<Character>> =
        dao.getAll()

    override suspend fun addCharacter(character: Character) =
        dao.insert(character)
}
