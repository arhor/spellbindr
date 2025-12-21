package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCharacterRepository(
    initialSheets: List<CharacterSheet> = emptyList(),
    initialCharacters: List<Character> = emptyList(),
) : CharacterRepository {
    private val sheetsState = MutableStateFlow(initialSheets.associateBy { it.id })
    private val charactersState = MutableStateFlow(initialCharacters.associateBy { it.id })

    override fun observeCharacterSheets(): Flow<List<CharacterSheet>> =
        sheetsState.map { it.values.toList() }

    override fun observeCharacterSheet(id: String): Flow<CharacterSheet?> =
        sheetsState.map { it[id] }

    override suspend fun upsertCharacterSheet(sheet: CharacterSheet) {
        sheetsState.update { current -> current + (sheet.id to sheet) }
    }

    override fun getCharacters(): Flow<List<Character>> =
        charactersState.map { it.values.toList() }

    override fun getCharacter(id: String): Flow<Character?> =
        charactersState.map { it[id] }

    override suspend fun saveCharacter(character: Character) {
        charactersState.update { current -> current + (character.id to character) }
    }

    override suspend fun deleteCharacter(id: String) {
        sheetsState.update { current -> current - id }
        charactersState.update { current -> current - id }
    }
}
