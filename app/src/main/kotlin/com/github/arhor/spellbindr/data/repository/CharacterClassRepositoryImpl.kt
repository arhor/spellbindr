package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepositoryImpl @Inject constructor(
    characterClassesDataStore: CharacterClassAssetDataStore,
) : CharacterClassRepository {
    private val allClasses = characterClassesDataStore.data.map { it ?: emptyList() }

    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        allClasses.firstOrNull()
            ?.let { data ->
                data.filter { it.spellcasting != null }
                    .map { EntityRef(it.id) }
            }
            ?: emptyList()
}
