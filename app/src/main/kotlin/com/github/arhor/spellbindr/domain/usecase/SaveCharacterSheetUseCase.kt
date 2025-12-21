package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import javax.inject.Inject

class SaveCharacterSheetUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(sheet: CharacterSheet) {
        characterRepository.upsertCharacterSheet(sheet)
    }
}
