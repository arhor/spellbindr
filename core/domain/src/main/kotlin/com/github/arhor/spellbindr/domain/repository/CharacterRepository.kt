package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing character data.
 *
 * Handles both the raw "character sheet" data (user inputs) and the derived/complete "character"
 * domain models used for gameplay logic.
 */
interface CharacterRepository {
    /**
     * Observes all saved character sheets. Emits updates whenever the list changes.
     */
    fun observeCharacterSheets(): Flow<Loadable<List<CharacterSheet>>>

    /**
     * Observes a specific character sheet by [id]. Emits null if not found.
     */
    fun observeCharacterSheet(id: String): Flow<CharacterSheet?>

    /**
     * Observes a specific character sheet state by [id]. Emits null if not found.
     */
    fun observeCharacterSheetState(id: String): Flow<Loadable<CharacterSheet?>>

    /**
     * Inserts or updates a character sheet.
     * Use this for saving manual edits from the character editor.
     *
     * @param sheet The character sheet data to save.
     */
    suspend fun upsertCharacterSheet(sheet: CharacterSheet)

    /**
     * Observes all characters as fully realized domain models.
     * Note: This transformation may involve additional computation or mapping from the raw sheets.
     */
    fun getCharacters(): Flow<List<Character>>

    /**
     * Observes a specific character domain model by [id].
     */
    fun getCharacter(id: String): Flow<Character?>

    /**
     * Saves a character domain model.
     * Typically delegates to [upsertCharacterSheet] after reverse-mapping relevant fields.
     */
    suspend fun saveCharacter(character: Character)

    /**
     * Deletes a character (and its associated sheet) by [id].
     */
    suspend fun deleteCharacter(id: String)
}
