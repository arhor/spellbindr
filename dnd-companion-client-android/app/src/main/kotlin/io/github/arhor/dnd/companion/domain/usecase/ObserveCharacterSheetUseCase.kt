package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveCharacterSheetUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {

    operator fun invoke(id: String): Flow<Loadable<CharacterSheet?>> {
        return characterRepository.observeCharacterSheetState(id)
    }
}
