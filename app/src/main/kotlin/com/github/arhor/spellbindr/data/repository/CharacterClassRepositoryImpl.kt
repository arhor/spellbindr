package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepositoryImpl @Inject constructor(
    private val characterClassesDataStore: CharacterClassAssetDataStore,
) : CharacterClassRepository {
    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        when (val state = characterClassesDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Ready -> state.data
                .filter { it.spellcasting != null }
                .map { EntityRef(it.id) }

            is Loadable.Error -> emptyList()

            is Loadable.Loading -> emptyList()
        }
}
