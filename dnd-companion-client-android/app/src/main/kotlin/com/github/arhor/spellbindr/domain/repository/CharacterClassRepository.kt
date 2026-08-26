package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface CharacterClassRepository {

    val allCharacterClassesState: Flow<Loadable<List<CharacterClass>>>
    suspend fun findSpellcastingClassesRefs(): List<EntityRef>
}
