package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.repository.CharacterRepository

class DeleteCharacterUseCase(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(id: String) {
        characterRepository.deleteCharacter(id)
    }
}
