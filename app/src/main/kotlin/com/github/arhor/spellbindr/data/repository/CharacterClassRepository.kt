package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.datasource.local.assets.CharacterClassesAssetDataStore
import com.github.arhor.spellbindr.data.model.EntityRef
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class CharacterClassRepository @Inject constructor(
    characterClassesDataStore: CharacterClassesAssetDataStore,
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
