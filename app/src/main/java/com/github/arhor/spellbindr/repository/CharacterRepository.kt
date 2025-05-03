package com.github.arhor.spellbindr.repository

import kotlinx.coroutines.flow.Flow
import com.github.arhor.spellbindr.data.model.Character

interface CharacterRepository {
    fun getCharacters(): Flow<List<Character>>
    suspend fun addCharacter(character: Character)
}