package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import javax.inject.Inject

class SaveGuidedCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(result: CharacterCreationResult) {
        characterRepository.saveGuidedCharacter(result)
    }
}
