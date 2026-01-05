package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import com.github.arhor.spellbindr.utils.mapLoadable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSpellcastingClassesUseCase @Inject constructor(
    private val characterClassRepository: CharacterClassRepository,
) {
    operator fun invoke(): Flow<Loadable<List<CharacterClass>>> =
        characterClassRepository
            .allCharacterClassesState
            .mapLoadable { data -> data.filter { it.spellcasting != null } }
}
