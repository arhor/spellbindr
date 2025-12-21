package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.repository.CharacterRepository

class SaveCharacterSheetUseCase(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(sheet: CharacterSheet) {
        characterRepository.upsertCharacterSheet(sheet)
    }
}
