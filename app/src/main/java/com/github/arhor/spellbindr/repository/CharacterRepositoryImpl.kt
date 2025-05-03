package com.github.arhor.spellbindr.repository

import com.github.arhor.spellbindr.data.dao.CharacterDao
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val dao: CharacterDao,
) : CharacterRepository {

    override fun getCharacters(): Flow<List<Character>> =
        dao.getAll()

    override suspend fun addCharacter(character: com.github.arhor.spellbindr.data.model.Character) =
        dao.insert(character)
}
