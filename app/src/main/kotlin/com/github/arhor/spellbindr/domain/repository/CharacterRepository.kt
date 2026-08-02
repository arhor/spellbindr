package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Character
import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ApplyLevelUpResult
import com.github.arhor.spellbindr.domain.model.LevelUpPlan
import com.github.arhor.spellbindr.domain.model.LevelUpReferenceData
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
     * Observes a specific character sheet and its progression management state by [id].
     * Emits null if the character or its manual sheet is not found.
     */
    fun observeCharacterWithProgression(id: String): Flow<CharacterWithProgression?>

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
     * Saves a character created by the guided flow with managed progression in one transaction.
     */
    suspend fun saveGuidedCharacter(result: CharacterCreationResult)

    /** Atomically validates, materializes, and appends exactly one managed level. */
    suspend fun applyLevelUp(
        characterId: String,
        expectedTotalLevel: Int,
        plan: LevelUpPlan,
        referenceData: LevelUpReferenceData,
    ): ApplyLevelUpResult

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
