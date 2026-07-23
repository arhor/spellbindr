package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
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
