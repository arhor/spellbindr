package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import javax.inject.Inject

class DeleteCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(id: String) {
        characterRepository.deleteCharacter(id)
    }
}
