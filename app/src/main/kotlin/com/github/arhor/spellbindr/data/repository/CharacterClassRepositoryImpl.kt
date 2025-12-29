package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepositoryImpl @Inject constructor(
    private val characterClassesDataStore: CharacterClassAssetDataStore,
) : CharacterClassRepository {
    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        when (val state = characterClassesDataStore.data.first { it !is AssetState.Loading }) {
            is AssetState.Ready -> state.data
                .filter { it.spellcasting != null }
                .map { EntityRef(it.id) }

            is AssetState.Error -> emptyList()

            is AssetState.Loading -> emptyList()
        }
}
