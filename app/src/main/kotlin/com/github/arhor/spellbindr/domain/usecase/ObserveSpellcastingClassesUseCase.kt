package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import com.github.arhor.spellbindr.utils.filterNotNullBy
import com.github.arhor.spellbindr.utils.mapWhenReady
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveSpellcastingClassesUseCase @Inject constructor(
    private val characterClassRepository: CharacterClassRepository,
) {
    operator fun invoke(): Flow<Loadable<List<CharacterClass>>> =
        characterClassRepository
            .allCharacterClassesState
            .mapWhenReady { it.filterNotNullBy(CharacterClass::spellcasting) }
}
