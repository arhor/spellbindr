package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveCharacterSheetsUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): Flow<Loadable<List<CharacterSheet>>> =
        characterRepository.observeCharacterSheets()
}
