package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterWithProgression
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadCharacterWithProgressionUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(id: String): Flow<CharacterWithProgression?> =
        characterRepository.observeCharacterWithProgression(id)
}
