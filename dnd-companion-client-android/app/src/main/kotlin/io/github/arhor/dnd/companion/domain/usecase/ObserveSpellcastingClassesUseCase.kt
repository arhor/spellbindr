package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.mapContent
import io.github.arhor.dnd.companion.domain.repository.CharacterClassRepository
import io.github.arhor.dnd.companion.utils.filterNotNullBy
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
            .mapContent { it.filterNotNullBy(CharacterClass::spellcasting) }
}
