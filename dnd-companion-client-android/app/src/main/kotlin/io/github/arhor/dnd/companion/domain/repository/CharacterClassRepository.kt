package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.CharacterClass
import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface CharacterClassRepository {

    val allCharacterClassesState: Flow<Loadable<List<CharacterClass>>>
    suspend fun findSpellcastingClassesRefs(): List<EntityRef>
}
