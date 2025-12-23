package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Use case for saving changes to a character sheet.
 */
class SaveCharacterSheetUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    /**
     * Persists the provided [sheet] to local storage.
     * This operation is idempotent (upsert).
     *
     * @param sheet The character sheet to save.
     */
    suspend operator fun invoke(sheet: CharacterSheet) {
        characterRepository.upsertCharacterSheet(sheet)
    }
}
