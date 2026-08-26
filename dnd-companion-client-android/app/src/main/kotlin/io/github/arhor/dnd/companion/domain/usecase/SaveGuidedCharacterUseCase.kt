package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterCreationResult
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import javax.inject.Inject

class SaveGuidedCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(result: CharacterCreationResult) {
        characterRepository.saveGuidedCharacter(result)
    }
}
