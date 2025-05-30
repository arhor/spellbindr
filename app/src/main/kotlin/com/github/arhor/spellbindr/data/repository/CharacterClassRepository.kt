package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.datasource.local.CharacterClassesAssetDataStore
import com.github.arhor.spellbindr.data.model.EntityRef
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepository @Inject constructor(
    characterClassesDataStore: CharacterClassesAssetDataStore,
) {
    val allRaces = characterClassesDataStore.data.map { it ?: emptyList() }

    suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        allRaces.firstOrNull()
            ?.let { data ->
                data.filter { it.spellcasting != null }
                    .map { EntityRef(it.id) }
            }
            ?: emptyList()
}
