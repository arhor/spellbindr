package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import kotlinx.coroutines.flow.Flow

interface CharactersRepository {
    fun observeCharacterSheets(): Flow<List<CharacterSheet>>

    fun observeCharacterSheet(id: String): Flow<CharacterSheet?>

    suspend fun upsertCharacterSheet(sheet: CharacterSheet)

    fun getCharacters(): Flow<List<Character>>

    fun getCharacter(id: String): Flow<Character?>

    suspend fun saveCharacter(character: Character)

    suspend fun deleteCharacter(id: String)
}
