package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepositoryImpl @Inject constructor(
    private val characterClassesDataStore: CharacterClassAssetDataStore,
) : CharacterClassRepository {

    override val allCharacterClassesState: Flow<Loadable<List<CharacterClass>>>
        get() = characterClassesDataStore.data

    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        when (val state = characterClassesDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data
                .filter { it.spellcasting != null }
                .map { EntityRef(it.id) }

            is Loadable.Failure -> emptyList()

            is Loadable.Loading -> emptyList()
        }
}
