package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for loading a specific character sheet for editing or viewing.
 */
class LoadCharacterSheetUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    /**
     * Returns a Flow observing the character sheet with the given [id].
     *
     * @param id The unique identifier of the character.
     * @return A Flow emitting the [CharacterSheet], or null if not found.
     */
    operator fun invoke(id: String): Flow<CharacterSheet?> = characterRepository.observeCharacterSheet(id)
}
