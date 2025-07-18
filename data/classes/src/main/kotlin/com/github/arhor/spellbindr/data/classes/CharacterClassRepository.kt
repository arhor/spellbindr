package com.github.arhor.spellbindr.data.classes

import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepository @Inject constructor(
    characterClassesDataStore: CharacterClassAssetDataStore,
) {
    val allClasses = characterClassesDataStore.data.map { it ?: emptyList() }

    suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        allClasses.firstOrNull()
            ?.let { data ->
                data.filter { it.spellcasting != null }
                    .map { EntityRef(it.id) }
            }
            ?: emptyList()
}
