package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class ObserveCharacterSheetsUseCase(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): Flow<List<CharacterSheet>> = characterRepository.observeCharacterSheets()
}
