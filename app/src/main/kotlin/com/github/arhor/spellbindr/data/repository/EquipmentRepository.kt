package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Equipment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class EquipmentRepository @Inject constructor(
    private val equipmentDataStore: EquipmentAssetDataStore,
) {
    val allEquipmentState: Flow<AssetState<List<Equipment>>>
        get() = equipmentDataStore.data

    suspend fun findEquipmentById(id: String): Equipment? =
        when (val state = equipmentDataStore.data.first { it !is AssetState.Loading }) {
            is AssetState.Ready -> state.data.find { it.id == id }
            is AssetState.Error -> null
            is AssetState.Loading -> null
        }
}
