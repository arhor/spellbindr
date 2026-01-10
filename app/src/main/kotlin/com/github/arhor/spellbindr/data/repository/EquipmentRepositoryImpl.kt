package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val equipmentDataStore: EquipmentAssetDataStore,
) : EquipmentRepository {

    override val allEquipmentState: Flow<Loadable<List<Equipment>>>
        get() = equipmentDataStore.data

    override suspend fun findEquipmentById(id: String): Equipment? =
        when (val state = equipmentDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data.find { it.id == id }
            is Loadable.Failure -> null
            is Loadable.Loading -> null
        }
}
