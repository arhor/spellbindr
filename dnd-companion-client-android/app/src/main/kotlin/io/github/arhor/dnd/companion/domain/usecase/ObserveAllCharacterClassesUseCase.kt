package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllCharacterClassesUseCase @Inject constructor(
    private val characterClassRepository: CharacterClassRepository,
) {
    operator fun invoke(): Flow<Loadable<List<CharacterClass>>> =
        characterClassRepository.allCharacterClassesState
}

