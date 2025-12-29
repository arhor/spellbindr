package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AssetState
import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepositoryImpl @Inject constructor(
    private val characterClassesDataStore: CharacterClassAssetDataStore,
) : CharacterClassRepository {
    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        characterClassesDataStore.data
            .filterIsInstance<AssetState.Ready<List<CharacterClass>>>()
            .map { it.data }
            .firstOrNull()
            ?.let { data ->
                data.filter { it.spellcasting != null }
                    .map { EntityRef(it.id) }
            }
            ?: emptyList()
}
